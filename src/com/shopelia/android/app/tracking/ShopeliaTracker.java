package com.shopelia.android.app.tracking;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.shopelia.android.app.tracking.UUIDManager.OnReceiveUuidListener;
import com.shopelia.android.concurent.ScheduledTask;
import com.shopelia.android.utils.TimeUnits;

class ShopeliaTracker extends Tracker {

    /**
     * Should run synchronously even for big operations (Delegate are not
     * running on UI thread but in thread pool)
     * 
     * @author Pierre Pollastri
     */
    interface FlushDelegate {
        /**
         * Called on proper thread
         * 
         * @param uuid The uuid of the phone
         * @param trackerName The tracker name
         * @param events The list of events
         * @return The lists of events that the {@link FlushDelegate} was not
         *         able to flush.
         */
        public List<ShopeliaEvent> flush(String uuid, String trackerName, ShopeliaEvent[] events);
    }

    private static final String PRIVATE_PREFERENCE = "Shopelia$Tracker.PrivatePreference";
    private static final String PREFS_VERSION = "tracker:version";
    private static final String PREFS_EVENTS = "tracker:events";

    private static final long EXPIRY_DELAY = 20 * TimeUnits.SECONDS;
    private static final long FLUSH_DELAY = 2 * TimeUnits.SECONDS;

    private static final int CURRENT_VERSION = 0;

    private static ShopeliaTracker sInstance;

    private FlushDelegate mFlushDelegate;
    private SerialExecutor mFlushExecutor = new SerialExecutor();
    private WeakReference<Context> mApplicationContext = new WeakReference<Context>(null);
    private ScheduledTask mFlushTask = new ScheduledTask();

    /**
     * Must be synchronized everywhere
     */
    private HashMap<String, HashSet<ShopeliaEvent>> mEvents = new HashMap<String, HashSet<ShopeliaEvent>>();

    private ShopeliaTracker() {
        setFlushDelegate(mPrivateFlushDelegate);
    }

    public void setFlushDelegate(FlushDelegate delegate) {
        if (delegate == null) {
            delegate = mPrivateFlushDelegate;
        }
        mFlushDelegate = delegate;
    }

    @Override
    public void init(Context context) {
        mApplicationContext = new WeakReference<Context>(context.getApplicationContext());
        load(context.getApplicationContext());
    }

    @Override
    public void flush() {
        requestFlush();
    }

    @Override
    public void onDisplayShopeliaButton(String url, String tracker) {
        addShopeliaEvent(tracker, new ShopeliaEvent(Actions.DISPLAY, url));
        save();
        requestFlush();
    }

    @Override
    public void onClickShopeliaButton(String url, String tracker) {
        addShopeliaEvent(tracker, new ShopeliaEvent(Actions.CLICK, url));
        save();
        requestFlush();
    }

    public static ShopeliaTracker getInstance() {
        return sInstance == null ? sInstance = new ShopeliaTracker() : sInstance;
    }

    private void addShopeliaEvent(String tracker, ShopeliaEvent event) {
        synchronized (mEvents) {
            HashSet<ShopeliaEvent> events = mEvents.get(tracker);
            if (events == null) {
                events = new HashSet<ShopeliaEvent>();
                mEvents.put(tracker, events);
            }
            if (events.contains(event)) {
                updateEvent(events, event);
            } else {
                events.add(event);
            }
        }
        mFlushTask.schedule(new Runnable() {

            @Override
            public void run() {
                requestFlush();
            }
        }, FLUSH_DELAY);
    }

    private void requestFlush() {
        UUIDManager.obtainUuid(new OnReceiveUuidListener() {

            @Override
            public void onReceiveUuid(final String uuid) {
                mFlushExecutor.execute(new Runnable() {

                    @Override
                    public void run() {
                        // Loop on entries
                        Map<String, ShopeliaEvent[]> events = getReadOnlyVersion(mEvents);
                        for (Map.Entry<String, ShopeliaEvent[]> event : events.entrySet()) {
                            List<ShopeliaEvent> notSent = mFlushDelegate.flush(uuid, event.getKey(), event.getValue());
                            // Update sent entries
                            synchronized (mEvents) {
                                HashSet<ShopeliaEvent> e = mEvents.get(event.getKey());
                                for (ShopeliaEvent ev : e) {
                                    if (notSent == null || !notSent.contains(ev)) {
                                        ev.sent_at = System.currentTimeMillis();
                                    }
                                }
                            }
                        }
                        save();
                    }
                });
            }
        });
    }

    private Map<String, ShopeliaEvent[]> getReadOnlyVersion(Map<String, HashSet<ShopeliaEvent>> events) {
        Map<String, ShopeliaEvent[]> out = new HashMap<String, ShopeliaEvent[]>(events.size());
        synchronized (events) {
            for (Entry<String, HashSet<ShopeliaEvent>> event : events.entrySet()) {
                ShopeliaEvent[] entries = new ShopeliaEvent[event.getValue().size()];
                event.getValue().toArray(entries);
                out.put(event.getKey(), entries);
            }
        }
        return out;
    }

    private void save() {
        Context context = mApplicationContext.get();
        if (context != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PRIVATE_PREFERENCE, Context.MODE_PRIVATE).edit();
            synchronized (mEvents) {
                try {
                    JSONObject eventsObject = new JSONObject();
                    for (Map.Entry<String, HashSet<ShopeliaEvent>> entry : mEvents.entrySet()) {
                        JSONArray entryArray = new JSONArray();
                        final long now = System.currentTimeMillis();
                        for (ShopeliaEvent event : entry.getValue()) {
                            if (event.sent_at + EXPIRY_DELAY <= now && event.sent_at != ShopeliaEvent.NEVER_SENT) {
                                entryArray.put(event.toJson());
                            }
                        }
                        eventsObject.put(entry.getKey(), entryArray);
                    }
                    editor.putInt(PREFS_VERSION, CURRENT_VERSION);
                    editor.putString(PREFS_EVENTS, eventsObject.toString());
                } catch (JSONException e) {

                }
            }
            editor.commit();
        }
    }

    private void load(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PRIVATE_PREFERENCE, Context.MODE_PRIVATE);
        if (preferences.contains(PREFS_VERSION) && preferences.contains(PREFS_EVENTS)) {
            synchronized (mEvents) {
                int version = preferences.getInt(PREFS_VERSION, CURRENT_VERSION);
                try {
                    JSONObject eventsObject = new JSONObject(preferences.getString(PREFS_EVENTS, ""));
                    JSONArray names = eventsObject.names();
                    final int size = names.length();
                    for (int index = 0; index < size; index++) {
                        JSONArray eventArray = eventsObject.getJSONArray(names.getString(index));
                        HashSet<ShopeliaEvent> events = new HashSet<ShopeliaEvent>(eventArray.length());
                        final int eventArrayLength = eventArray.length();
                        for (int indexEventArray = 0; indexEventArray < eventArrayLength; index++) {
                            events.add(ShopeliaEvent.inflate(eventArray.getJSONObject(indexEventArray)));
                        }
                        mEvents.put(names.getString(index), events);
                    }
                } catch (JSONException e) {

                }
                if (version != CURRENT_VERSION) {
                    migrate(version, CURRENT_VERSION);
                }
            }
        }
    }

    public void migrate(int from, int to) {

    }

    private static void updateEvent(HashSet<ShopeliaEvent> events, ShopeliaEvent update) {
        for (ShopeliaEvent event : events) {
            if (event.equals(update)) {
                event.update(event);
                if (event.sent_at == ShopeliaEvent.NEVER_SENT || event.sent_at + EXPIRY_DELAY <= System.currentTimeMillis()) {
                    event.sent_at = ShopeliaEvent.NEVER_SENT;
                }
                break;
            }
        }
    }

    private interface Actions {
        String CLICK = "click";
        String DISPLAY = "view";
    }

    private FlushDelegate mPrivateFlushDelegate = new FlushDelegate() {

        @Override
        public List<ShopeliaEvent> flush(String uuid, String trackerName, ShopeliaEvent[] events) {
            final long now = System.currentTimeMillis();
            for (ShopeliaEvent event : events) {
                if (event.sent_at == ShopeliaEvent.NEVER_SENT || event.sent_at + EXPIRY_DELAY <= now) {
                    Log.d(null, "TRACKING " + event.action + " " + event.url);
                }
            }
            return null;
        }
    };

}
