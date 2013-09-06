package com.shopelia.android.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import com.shopelia.android.config.Config;
import com.shopelia.android.model.JsonData;

public final class JsonUtils {

    public interface OnObjectParsedListener<T> {
        public void onObjectParsed(T object);

        public void onException(JSONException e);
    }

    private JsonUtils() {

    }

    public static <T extends JsonData> JSONArray toJson(List<T> objects) {
        JSONArray array = new JSONArray();
        for (T object : objects) {
            try {
                array.put(object.toJson());
            } catch (JSONException e) {
                if (Config.ERROR_LOGS_ENABLED) {
                    e.printStackTrace();
                }
            }
        }
        return array;
    }

    public static <T extends JsonData> JSONObject toJson(Map<String, T> map) {
        JSONObject out = new JSONObject();
        for (Map.Entry<String, T> entry : map.entrySet()) {
            try {
                out.put(entry.getKey(), entry.getValue().toJson());
            } catch (JSONException e) {

            }
        }
        return out;
    }

    public static <T extends JsonData> JSONArray toJson(Set<T> set) {
        JSONArray out = new JSONArray();
        for (T entry : set) {
            try {
                out.put(entry.toJson());
            } catch (JSONException e) {

            }
        }
        return out;
    }

    public static void mergeObject(JSONObject root, String key, JSONObject toMerge) throws JSONException {
        if (!root.has(key)) {
            root.put(key, toMerge);
        } else {
            JSONObject dest = root.getJSONObject(key);
            JSONArray names = toMerge.names();
            final int count = names.length();
            for (int index = 0; index < count; index++) {
                String name = names.getString(index);
                Object object = toMerge.get(name);
                if (object instanceof JSONObject) {
                    mergeObject(dest, name, (JSONObject) object);
                } else {
                    dest.put(name, object);
                }
            }
        }
    }

    /**
     * Inserts a new key/value in the dest {@link JSONObject}. If dest is null,
     * the method will create a new {@link JSONObject} and return it.
     * 
     * @param dest
     * @param key
     * @param value
     * @return
     * @throws JSONException
     */
    public static JSONObject insert(JSONObject dest, String key, Object value) throws JSONException {
        if (dest == null) {
            dest = new JSONObject();
        }
        dest.put(key, value);
        return dest;
    }

    @SuppressLint("NewApi")
    public static void parseObjectAsync(final CharSequence charSequence, final OnObjectParsedListener<JSONObject> l) {
        AsyncTask<Void, Void, JSONObject> task = (new AsyncTask<Void, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(Void... params) {
                try {
                    return new JSONObject(charSequence.toString());
                } catch (JSONException e) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                super.onPostExecute(result);
                l.onObjectParsed(result);
            }

        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }
}
