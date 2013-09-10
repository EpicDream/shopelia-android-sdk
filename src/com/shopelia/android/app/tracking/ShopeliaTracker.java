package com.shopelia.android.app.tracking;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.shopelia.android.app.tracking.UUIDManager.OnReceiveUuidListener;
import com.shopelia.android.concurent.ScheduledTask;
import com.shopelia.android.config.Config;
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.shopelia.android.utils.TimeUnits;
import com.turbomanage.httpclient.HttpResponse;

class ShopeliaTracker extends Tracker {

    /**
     * Should run synchronously even for big operations (Delegate are not
     * running on UI thread but in thread pool)
     * 
     * @author Pierre Pollastri
     */
    interface FlushDelegate {
        public boolean send(String uuid, String tracker, String action, ArrayList<ShopeliaEvent> events);
    }

    private static final String LOG = "ShopeliaTracker";

    private static final String PRIVATE_PREFERENCE = "Shopelia$Tracker.PrivatePreference";
    private static final String PREFS_VERSION = "tracker:version";
    private static final String PREFS_EVENTS = "tracker:events";
    private static final String DEFAULT_TRACKER_NAME = "android-sdk";

    private static final long EXPIRY_DELAY = 20 * TimeUnits.MINUTES;
    private static final long FLUSH_DELAY = 2 * TimeUnits.SECONDS;

    private static final int CURRENT_VERSION = 0;

    private static ShopeliaTracker sInstance;

    private FlushDelegate mFlushDelegate;
    private SerialExecutor mFlushExecutor = new SerialExecutor();
    private WeakReference<Context> mApplicationContext = new WeakReference<Context>(null);
    private ScheduledTask mFlushTask = new ScheduledTask(new Handler(Looper.getMainLooper()));
    private long mExpiryDelay = EXPIRY_DELAY;

    /**
     * Must be synchronized everywhere
     */
    private HashMap<String, HashSet<ShopeliaEvent>> mEvents;

    private ShopeliaTracker() {
        setFlushDelegate(mPrivateFlushDelegate);
    }

    void setExpiryDelay(long expiryDelay) {
        mExpiryDelay = expiryDelay;
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
    }

    @Override
    public void onClickShopeliaButton(String url, String tracker) {
        addShopeliaEvent(tracker, new ShopeliaEvent(Actions.CLICK, url));
    }

    public static ShopeliaTracker getInstance() {
        return sInstance == null ? sInstance = new ShopeliaTracker() : sInstance;
    }

    private void addShopeliaEvent(String tracker, ShopeliaEvent event) {
        if (mEvents == null) {
            return;
        }
        tracker = !TextUtils.isEmpty(tracker) ? tracker : DEFAULT_TRACKER_NAME;
        synchronized (mEvents) {
            event.request++;
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
        if (mEvents == null) {
            return;
        }
        UUIDManager.obtainUuid(new OnReceiveUuidListener() {

            @Override
            public void onReceiveUuid(final String uuid) {
                mFlushExecutor.execute(new Runnable() {

                    @Override
                    public void run() {
                        // Loop on entries
                        Map<String, ShopeliaEvent[]> events = getReadOnlyVersion(mEvents);
                        for (Map.Entry<String, ShopeliaEvent[]> event : events.entrySet()) {
                            List<ShopeliaEvent> notSent = flush(uuid, event.getKey(), event.getValue());
                            // Update sent entries
                            synchronized (mEvents) {
                                HashSet<ShopeliaEvent> e = mEvents.get(event.getKey());
                                for (ShopeliaEvent ev : e) {
                                    if (notSent == null || !notSent.contains(ev)) {
                                        ev.sent_at = System.currentTimeMillis();
                                        ev.request = 0;
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
        if (mEvents == null) {
            return;
        }
        Context context = mApplicationContext.get();
        if (context != null && mEvents != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PRIVATE_PREFERENCE, Context.MODE_PRIVATE).edit();
            synchronized (mEvents) {
                try {
                    JSONObject eventsObject = new JSONObject();
                    for (Map.Entry<String, HashSet<ShopeliaEvent>> entry : mEvents.entrySet()) {
                        if (entry.getKey() != null) {
                            JSONArray entryArray = new JSONArray();
                            final long now = System.currentTimeMillis();
                            for (ShopeliaEvent event : entry.getValue()) {
                                if (event.sent_at + mExpiryDelay >= now && event.sent_at != ShopeliaEvent.NEVER_SENT) {
                                    entryArray.put(event.toJson());
                                }
                            }
                            eventsObject.put(entry.getKey(), entryArray);
                        }
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
        if (mEvents != null) {
            return;
        }
        mEvents = new HashMap<String, HashSet<ShopeliaEvent>>();
        SharedPreferences preferences = context.getSharedPreferences(PRIVATE_PREFERENCE, Context.MODE_PRIVATE);
        if (preferences.contains(PREFS_VERSION) && preferences.contains(PREFS_EVENTS)) {
            synchronized (mEvents) {
                int version = preferences.getInt(PREFS_VERSION, CURRENT_VERSION);
                try {
                    JSONObject eventsObject = new JSONObject(preferences.getString(PREFS_EVENTS, ""));
                    JSONArray names = eventsObject.names() != null ? eventsObject.names() : new JSONArray();
                    final int size = names.length();
                    for (int index = 0; index < size; index++) {
                        JSONArray eventArray = eventsObject.getJSONArray(names.getString(index));
                        HashSet<ShopeliaEvent> events = new HashSet<ShopeliaEvent>(eventArray.length());
                        final int eventArrayLength = eventArray.length();
                        for (int indexEventArray = 0; indexEventArray < eventArrayLength; indexEventArray++) {
                            ShopeliaEvent loaded = ShopeliaEvent.inflate(eventArray.getJSONObject(indexEventArray));
                            events.add(loaded);
                        }
                        mEvents.put(names.getString(index), events);
                    }
                } catch (Exception e) {

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
                event.update(update);
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
        public boolean send(String uuid, String tracker, String action, ArrayList<ShopeliaEvent> events) {
            JSONArray urls = toJsonArray(events);
            Context context = mApplicationContext.get();
            if (urls.length() == 0 || context == null) {
                return false;
            }
            JSONObject params = new JSONObject();
            try {
                params.put(Api.TRACKER, tracker);
                params.put(Api.TYPE, action);
                params.put(Api.URLS, urls);
                params.put(Api.VISITOR, uuid);
                if (Config.DEBUG) {
                    Log.d(LOG, "Tracking for " + uuid);
                    Log.d(LOG, "Tracking " + params.toString(2));
                }
                HttpResponse response = ShopeliaRestClient.V1(context).post(Command.V1.Events.$, params);
                return response != null && response.getStatus() == 204;
            } catch (Exception e) {

            }
            return false;
        }

    };

    public List<ShopeliaEvent> flush(String uuid, String trackerName, ShopeliaEvent[] events) {
        final long now = System.currentTimeMillis();
        ArrayList<ShopeliaEvent> notSent = new ArrayList<ShopeliaEvent>(events.length);
        HashMap<String, ArrayList<ShopeliaEvent>> sortedByAction = new HashMap<String, ArrayList<ShopeliaEvent>>();

        for (ShopeliaEvent event : events) {
            if (event.request > 0 && (event.sent_at == ShopeliaEvent.NEVER_SENT || event.sent_at + mExpiryDelay <= now)) {
                if (!sortedByAction.containsKey(event.action)) {
                    sortedByAction.put(event.action, new ArrayList<ShopeliaEvent>(events.length));
                }
                sortedByAction.get(event.action).add(event);
            } else {
                notSent.add(event);
            }
        }

        sendAll(uuid, trackerName, sortedByAction, notSent);

        return notSent;
    }

    private void sendAll(String uuid, String tracker, HashMap<String, ArrayList<ShopeliaEvent>> sortedByAction,
            ArrayList<ShopeliaEvent> notSent) {
        for (Map.Entry<String, ArrayList<ShopeliaEvent>> entry : sortedByAction.entrySet()) {
            if (!mFlushDelegate.send(uuid, tracker, entry.getKey(), entry.getValue())) {
                notSent.addAll(entry.getValue());
            }
        }
    }

    private JSONArray toJsonArray(ArrayList<ShopeliaEvent> events) {
        JSONArray array = new JSONArray();
        for (ShopeliaEvent event : events) {
            array.put(event.url);
        }
        return array;
    }

    private interface Api {
        String URLS = "urls";
        String TRACKER = "tracker";
        String TYPE = "type";
        String VISITOR = "visitor";

    }

}
