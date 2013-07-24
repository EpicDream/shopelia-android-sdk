package com.shopelia.android.app.tracking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;

import com.shopelia.android.app.ShopeliaTracker;
import com.shopelia.android.model.JsonData;
import com.shopelia.android.utils.DigestUtils;
import com.shopelia.android.utils.IOUtils;
import com.shopelia.android.utils.QualifiedLists;
import com.shopelia.android.utils.QualifiedLists.Revokator;

public class VikingTracker extends ShopeliaTracker {

    private static VikingTracker sInstance;

    public static VikingTracker getInstance() {
        return sInstance != null ? sInstance : (sInstance = new VikingTracker());
    }

    private static final String DIRECTORY = "Shopelia/";
    private static final String SAVE_FILE = "internal.json";
    private static final String CHARSET = "UTF-8";

    private QualifiedLists<Entry> mData;
    private File mSaveFile;
    private String uuid;

    private VikingTracker() {

    }

    @Override
    public void onClickShopeliaButton(String url) {
        super.onClickShopeliaButton(url);
        mData.getList(Lists.EVENTS).add(new Entry(Actions.CLICK, url));
    }

    @Override
    public void onDisplayShopeliaButton(String url) {
        super.onDisplayShopeliaButton(url);
        mData.getList(Lists.EVENTS).add(new Entry(Actions.DISPLAY, url));
    }

    @Override
    public void init(Context context) {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            File dir = new File(Environment.getExternalStorageDirectory(), DIRECTORY);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            mSaveFile = new File(dir, SAVE_FILE);
            load();
        }
    }

    @Override
    public void flush() {
        ArrayList<Entry> entry = mData.diff(Lists.EVENTS, Lists.EVENTS_SENT);
        mData.merge(Lists.EVENTS, Lists.EVENTS_SENT);
        save();
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
        if (mSaveFile == null || !mSaveFile.exists()) {
            return;
        }
        mData.getList(Lists.EVENTS).clear();
        mData.revoke(Lists.EVENTS_SENT, new Revokator<VikingTracker.Entry>() {

            @Override
            public boolean revoke(Entry item) {
                return false;
            }

        });
    }

    public String getUuid() {
        return uuid;
    }

    private static class Entry implements JsonData {

        interface Api {
            String DIGEST = "digest";
            String CREATED_AT = "created_url";
        }

        long created_at;
        String digest;
        String action;
        String url;

        public Entry(String action, String url) {
            this.url = url;
            this.action = action;
            this.digest = DigestUtils.md5(action + "://" + url);
            created_at = System.currentTimeMillis();
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
        String UUID = "uuid";
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

    }

    @SuppressWarnings("unused")
    private Object mFinalizer = new Object() {

        @Override
        protected void finalize() throws Throwable {
            flush();
        }

    };

}
