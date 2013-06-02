package com.shopelia.android.utils;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shopelia.android.config.Config;
import com.shopelia.android.model.JsonData;

public final class JsonUtils {

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
     * Inserts a new key/value in the dest {@link JSONObject}. If dest is null, the method will create a new {@link JSONObject} and return it.
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
    
}
