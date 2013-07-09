package com.shopelia.android.app;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.app.tracking.MixPanelTracker;

public interface ShopeliaTracker {

    public int MIXPANEL = 0x1;

    public void init(Context context);

    public void track(String eventName);

    public void track(String eventName, JSONObject object);

    public void onDisplay(String activityName);

    public void onFocusIn(String fieldName);

    public void onValidate(String fieldName);

    public void flush();

    public static class Factory {

        public static ShopeliaTracker create(int provider) {
            switch (provider) {
                case MIXPANEL:
                    return new MixPanelTracker();
                default:
                    return new MixPanelTracker();
            }
        }

    }

    public static class Builder extends JSONObject {

        public static Builder create() {
            return new Builder();
        }

        public Builder add(String key, Object value) {
            try {
                put(key, value);
            } catch (JSONException e) {

            }
            return this;
        }

    }

}
