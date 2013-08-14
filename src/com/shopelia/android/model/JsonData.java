package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonData {

    public JSONObject toJson() throws JSONException;

    public interface JsonInflater<E> {

        public static final JsonInflater<String> STRING_INFLATER = new JsonInflater<String>() {

            @Override
            public String inflate(JSONObject source) throws JSONException {
                return null;
            }
        };

        public E inflate(JSONObject source) throws JSONException;
    }

}
