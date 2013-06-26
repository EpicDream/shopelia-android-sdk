package com.shopelia.android.remote.api;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Base64;

import com.shopelia.android.model.JsonData;
import com.turbomanage.httpclient.ParameterMap;

public class ShopeliaHttpSynchronizer {

    private static ShopeliaHttpSynchronizer sInstance;

    private ShopeliaHttpSynchronizer() {

    }

    public static void delete(String command, JSONObject params, String notificationId) {

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

        public static ArrayList<Query> inflate(JSONArray array) {
            final int size = array.length();
            ArrayList<Query> queries = new ArrayList<ShopeliaHttpSynchronizer.Query>(size);
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
    }

}
