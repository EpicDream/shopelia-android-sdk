package com.shopelia.android.app.tracking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.shopelia.android.app.ShopeliaTracker;
import com.shopelia.android.concurent.ScheduledTask;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.JsonData;
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.shopelia.android.utils.DigestUtils;
import com.shopelia.android.utils.IOUtils;
import com.shopelia.android.utils.MultiHashSet;
import com.shopelia.android.utils.MultiHashSet.Revokator;
import com.shopelia.android.utils.TimeUnits;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class VikingTracker extends ShopeliaTracker {

    private static final String LOG = "VikingTracker";

    public interface FlushDelegate {
        public void onFlush(HashSet<Entry> entries);
    }

    private static VikingTracker sInstance;

    public static VikingTracker getInstance() {
        return sInstance != null ? sInstance : (sInstance = new VikingTracker());
    }

    private static final String DIRECTORY = "Shopelia/";
    private static final String SAVE_FILE = "internal.json";
    private static final String CHARSET = "UTF-8";
    private static final long REVOCATION_DELAY = 20 * TimeUnits.MINUTES;
    private static final String DEFAULT_TRACKER_NAME = "Android";
    private static final long FLUSH_TASK_DELAY = 1 * TimeUnits.SECONDS;

    // private QualifiedLists<Entry> mData = new
    // QualifiedLists<VikingTracker.Entry>();
    private MultiHashSet<String, Entry> mData = new MultiHashSet<String, VikingTracker.Entry>();
    private File mSaveFile;
    private String mUuid;
    private HashSet<String> mTrakers = new HashSet<String>();
    private Context mContext;
    private ScheduledTask mFlushTask = new ScheduledTask();

    private FlushDelegate mDelegate;

    private VikingTracker() {
        mTrakers.add(DEFAULT_TRACKER_NAME);
    }

    public void setFlushDelegate(FlushDelegate delegate) {
        mDelegate = delegate;
    }

    @Override
    public void onClickShopeliaButton(String url, String tracker) {
        if (tracker == null) {
            tracker = DEFAULT_TRACKER_NAME;
        }
        addEventTolist(Lists.EVENTS, new Entry(Actions.CLICK, url, tracker));
        mFlushTask.cancel();
        mFlushTask.schedule(mFlushRunnable, FLUSH_TASK_DELAY);
    }

    @Override
    public void onDisplayShopeliaButton(String url, String tracker) {
        if (tracker == null) {
            tracker = DEFAULT_TRACKER_NAME;
        }
        addEventTolist(Lists.EVENTS, new Entry(Actions.DISPLAY, url, tracker));
        mFlushTask.cancel();
        mFlushTask.schedule(mFlushRunnable, FLUSH_TASK_DELAY);
    }

    private synchronized void addEventTolist(String name, Entry entry) {
        mTrakers.add(entry.tracker);
        mData.put(name, entry);
    }

    @Override
    public void init(Context context) {
        if (mContext == null) {
            File dir = new File(Environment.getExternalStorageDirectory(), DIRECTORY);
            dir.mkdirs();
            mSaveFile = new File(dir, SAVE_FILE);
            load();
            mContext = context.getApplicationContext();
        }
    }

    @Override
    public synchronized void flush() {
        mData.revoke(Lists.EVENTS_SENT, new Revokator<Entry>() {

            @Override
            public boolean revoke(Entry item) {
                return (System.currentTimeMillis() - item.created_at) > REVOCATION_DELAY;
            }

        });
        HashSet<Entry> entries = mData.diff(Lists.EVENTS, Lists.EVENTS_SENT);
        mData.merge(Lists.EVENTS, Lists.EVENTS_SENT);
        save();
        if (mDelegate != null) {
            mDelegate.onFlush(entries);
        } else {
            for (String tracker : mTrakers) {
                sendEventsToTracker(entries, tracker);
            }
        }
    }

    private void sendEventsToTracker(HashSet<Entry> entries, String tracker) {
        sendEventToTracker(entries, tracker, Actions.CLICK);
        sendEventToTracker(entries, tracker, Actions.DISPLAY);
    }

    private void sendEventToTracker(final HashSet<Entry> entries, String tracker, String action) {
        JSONArray urls = prepareArray(entries, action, tracker);
        if (urls.length() == 0) {
            return;
        }
        JSONObject params = new JSONObject();
        try {
            params.put(Api.TRACKER, tracker);
            params.put(Api.TYPE, action);
            params.put(Api.URLS, urls);
            params.put(Api.VISITOR, getUuid());
            if (Config.DEBUG) {
                Log.d(LOG, "Tracking " + params.toString(2));
            }
            ShopeliaRestClient.V1(mContext).post(Command.V1.Events.$, params, new AsyncCallback() {

                @Override
                public void onComplete(HttpResponse httpResponse) {
                    // Do nothing
                }

                @Override
                public void onError(Exception e) {
                    super.onError(e);
                    mData.revert(Lists.EVENTS, Lists.EVENTS_SENT);
                }

            });
        } catch (JSONException e) {

        }
    }

    private JSONArray prepareArray(HashSet<Entry> entries, String action, String tracker) {
        JSONArray array = new JSONArray();
        for (Entry entry : entries) {
            Log.d(null, "ENTRY " + entry.action + " " + entry.url);
            if (action.equals(entry.action) && tracker.equals(entry.tracker)) {
                array.put(entry.url);
            }
        }
        return array;
    }

    public void reset() {
        if (mSaveFile != null) {
            mSaveFile.delete();
        }
        load();
    }

    protected void save() {
        if (mSaveFile == null) {
            return;
        }
        try {
            JSONObject object = mData.toJson();
            StringReader reader = new StringReader(object.toString());
            IOUtils.copy(reader, new FileOutputStream(mSaveFile), Charset.forName(CHARSET));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void load() {
        if (mSaveFile != null && mSaveFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(mSaveFile);
                StringWriter writer = new StringWriter();
                IOUtils.copy(fis, writer, Charset.forName(CHARSET));
                JSONObject object = new JSONObject(writer.toString());
                mData = MultiHashSet.inflate(object, Entry.INFLATOR);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(fis);
            }
            mData.getSet(Lists.EVENTS).clear();
        } else {
            mData = new MultiHashSet<String, VikingTracker.Entry>();
        }
        HashSet<Entry> uuids = mData.getSet(Lists.UUIDS);
        if (uuids.size() == 0) {
            mUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 32).toLowerCase();
            uuids.add(new Entry(mUuid));
        } else {
            mUuid = uuids.iterator().next().digest;
        }
    }

    public String getUuid() {
        return mUuid;
    }

    public static class Entry implements JsonData {

        interface Api {
            String DIGEST = "digest";
            String CREATED_AT = "created_at";
        }

        public long created_at;
        public String digest;
        public String action;
        public String url;
        public String tracker;

        public Entry(String digest) {
            this.digest = digest;
            created_at = System.currentTimeMillis();
        }

        public Entry(String action, String url, String tracker) {
            this.url = url;
            this.action = action;
            this.digest = DigestUtils.md5(action + "://" + url);
            created_at = System.currentTimeMillis();
            this.tracker = tracker;
        }

        protected Entry(JSONObject source) throws JSONException {
            created_at = source.getLong(Api.CREATED_AT);
            digest = source.getString(Api.DIGEST);
        }

        @Override
        public JSONObject toJson() throws JSONException {
            JSONObject object = new JSONObject();
            object.put(Api.CREATED_AT, created_at);
            object.put(Api.DIGEST, digest);
            return object;
        }

        public static final JsonData.JsonInflater<Entry> INFLATOR = new JsonInflater<VikingTracker.Entry>() {

            @Override
            public Entry inflate(JSONObject source) throws JSONException {
                return new Entry(source);
            }
        };

        @Override
        public boolean equals(Object o) {
            if (o instanceof Entry) {
                return ((Entry) o).digest.equals(digest);
            }
            return false;
        }

    }

    private interface Lists {
        String UUIDS = "uuids";
        String EVENTS_SENT = "events_sent";
        String EVENTS = "events";
    }

    private interface Actions {
        String CLICK = "click";
        String DISPLAY = "view";
    }

    private interface Api {
        String URLS = "urls";
        String TRACKER = "tracker";
        String TYPE = "type";
        String VISITOR = "visitor";

    }

    @SuppressWarnings("unused")
    private Object mFinalizer = new Object() {

        @Override
        protected void finalize() throws Throwable {
            flush();
        }

    };

    private Runnable mFlushRunnable = new Runnable() {

        @Override
        public void run() {
            flush();
        }
    };

}
