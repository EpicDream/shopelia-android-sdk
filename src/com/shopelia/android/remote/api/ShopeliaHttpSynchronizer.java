package com.shopelia.android.remote.api;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.shopelia.android.config.Config;
import com.shopelia.android.model.JsonData;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

public final class ShopeliaHttpSynchronizer {

    public static final String PREFERENCES = "ShopeliaHttpSynchronizer";
    public static final String LOG_TAG = "ShopeliaHttpSynchronizer";

    private static ShopeliaHttpSynchronizer sInstance;

    private Context mContext;
    private LinkedList<Query> mQueries = new LinkedList<ShopeliaHttpSynchronizer.Query>();
    private boolean mIsFlushing = false;

    private ShopeliaHttpSynchronizer() {
        load();
    }

    private static ShopeliaHttpSynchronizer getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ShopeliaHttpSynchronizer();
        }
        sInstance.attach(context);
        return sInstance;
    }

    public static void reset(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Config.PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(PREFERENCES, "");
        editor.commit();
        getInstance(context).mQueries.clear();
    }

    public static void delete(Context context, String command, JSONObject params, String notificationId) {
        ShopeliaHttpSynchronizer synchronizer = getInstance(context);
        synchronizer.addQuery(new Query(Query.METHOD_DELETE, notificationId, command, params));
        synchronizer.flush();
    }

    public static void put(Context context, String command, JSONObject params, String notificationId) {
        ShopeliaHttpSynchronizer synchronizer = getInstance(context);
        synchronizer.addQuery(new Query(Query.METHOD_PUT, notificationId, command, params));
        synchronizer.flush();
    }

    public static void flush(Context context) {
        getInstance(context).flush();
    }

    private void flush() {
        if (mIsFlushing) {
            return;
        }
        if (mQueries.size() == 0) {
            detach();
            return;
        }
        mIsFlushing = true;
        final Query query = mQueries.getFirst();
        query.execute(getContext(), new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {
                synchronized (mQueries) {
                    mQueries.removeFirst();
                }
                notifyQuerySent(query);
                mIsFlushing = false;
                flush();
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
            }

        });
    }

    private void notifyQuerySent(Query query) {
        if (Config.INFO_LOGS_ENABLED) {
            Log.i(LOG_TAG, "Query sent : \n" + query.toString());
            Log.i(LOG_TAG, "Remaining queries : " + mQueries.size());
        }
    }

    public void addQuery(Query query) {
        synchronized (mQueries) {
            mQueries.add(query);
        }
    }

    private void attach(Context context) {
        mContext = context.getApplicationContext();
    }

    public void save() {
        try {
            JSONArray array = new JSONArray();
            for (Query query : mQueries) {
                array.put(query.toJson());
            }
            SharedPreferences.Editor editor = getContext().getSharedPreferences(Config.PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(PREFERENCES, array.toString());
            editor.commit();
        } catch (JSONException e) {

        }
    }

    public void load() {
        if (mQueries.size() == 0) {
            SharedPreferences preferences = getContext().getSharedPreferences(Config.PREFERENCES_NAME, Context.MODE_PRIVATE);
            String array = preferences.getString(PREFERENCES, "").trim();
            if (!TextUtils.isEmpty(array)) {
                try {
                    mQueries = Query.inflate(new JSONArray(array));
                } catch (Exception e) {

                }
            }
        }
    }

    private void detach() {
        mContext = null;
    }

    public Context getContext() {
        return mContext;
    }

    private static class Query implements JsonData {

        private interface Key {
            String METHOD = "method";
            String NOTIFICATION_ID = "notificationId";
            String COMMAND = "command";
            String OBJECT = "object";
            String CONTENT_TYPE = "contentType";
            String DATA = "data";
            // String PARAMS = "params";
        }

        public static final String METHOD_DELETE = "DELETE";
        public static final String METHOD_POST = "POST";
        public static final String METHOD_PUT = "PUT";

        public String method;
        public String notificationId;
        public String command;

        public JSONObject object;

        public String contentType;
        public byte[] data;

        public ParameterMap params;

        public Query() {

        }

        public Query(String method, String notificationId, String command, JSONObject object) {
            this(method, notificationId, command, object, null, null, null);
        }

        public Query(String method, String notificationId, String command, String contentType, byte[] data) {
            this(method, notificationId, command, null, contentType, data, null);
        }

        public Query(String method, String notificationId, String command, ParameterMap params) {
            this(method, notificationId, command, null, null, null, params);
        }

        public Query(String method, String notificationId, String command, JSONObject object, String contentType, byte[] data,
                ParameterMap params) {
            this.method = method;
            this.notificationId = notificationId;
            this.command = command;
            this.object = object;
            this.contentType = contentType;
            this.data = data;
            this.params = params;
        }

        public void execute(Context context, AsyncCallback callback) {
            if (method.equals(METHOD_DELETE)) {
                delete(context, callback);
            } else if (method.equals(METHOD_POST)) {
                post(context, callback);
            } else if (method.equals(METHOD_PUT)) {
                put(context, callback);
            }
        }

        protected void put(Context context, AsyncCallback callback) {
            ShopeliaRestClient.authenticate(context);
            if (object != null) {
                ShopeliaRestClient.put(command, object, callback);
            }
        }

        protected void delete(Context context, AsyncCallback callback) {
            ShopeliaRestClient.authenticate(context);
            ShopeliaRestClient.delete(command, params, callback);
        }

        protected void post(Context context, AsyncCallback callback) {
            ShopeliaRestClient.authenticate(context);
            if (object != null) {
                ShopeliaRestClient.post(command, object, callback);
            }
        }

        @SuppressLint("NewApi")
        public static Query inflate(JSONObject object) throws JSONException {
            Query out = new Query();
            out.method = object.getString(Key.METHOD);
            out.notificationId = object.getString(Key.NOTIFICATION_ID);
            out.command = object.getString(Key.COMMAND);
            out.object = object.optJSONObject(Key.OBJECT);
            out.contentType = object.optString(Key.CONTENT_TYPE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO && object.optString(Key.DATA) != null) {
                out.data = Base64.decode(object.optString(Key.DATA), 0);
            }
            // TODO : Unserialize ParameterMap
            return out;
        }

        public static LinkedList<Query> inflate(JSONArray array) {
            final int size = array.length();
            LinkedList<Query> queries = new LinkedList<ShopeliaHttpSynchronizer.Query>();
            for (int index = 0; index < size; index++) {
                try {
                    queries.add(Query.inflate(array.optJSONObject(index)));
                } catch (JSONException e) {

                }
            }
            return queries;
        }

        @SuppressLint("NewApi")
        @Override
        public JSONObject toJson() throws JSONException {
            JSONObject out = new JSONObject();
            out.put(Key.METHOD, method);
            out.put(Key.NOTIFICATION_ID, notificationId);
            out.put(Key.COMMAND, command);
            out.put(Key.OBJECT, object);
            out.put(Key.CONTENT_TYPE, contentType);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO && data != null) {
                out.put(Key.DATA, Base64.encode(data, 0));
            }
            // TODO : Serialize ParameterMap
            return out;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(method).append(" ").append(command).append("\n");
            return builder.toString();
        }

    }

}
