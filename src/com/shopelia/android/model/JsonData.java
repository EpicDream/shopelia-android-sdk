package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonData {

    public JSONObject toJson() throws JSONException;

    public interface JsonInflater<E> {
        public E inflate(JSONObject source) throws JSONException;
    }

}
