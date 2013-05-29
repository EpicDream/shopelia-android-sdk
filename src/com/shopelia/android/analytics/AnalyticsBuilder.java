package com.shopelia.android.analytics;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class AnalyticsBuilder extends JSONObject {

    private Context mContext;

    public AnalyticsBuilder(Context context) {
        mContext = context;
    }

    public static AnalyticsBuilder prepareStepPackage(Context context, String stepName) {
        AnalyticsBuilder builder = new AnalyticsBuilder(context);
        builder.add(Analytics.Properties.STEP, stepName);
        return builder;
    }

    public AnalyticsBuilder add(String property, Object value) {
        try {
            put(property, value);
        } catch (JSONException e) {

        }
        return this;
    }

    public JSONObject build() {
        return this;
    }

}
