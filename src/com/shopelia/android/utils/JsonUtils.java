package com.shopelia.android.utils;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

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

}
