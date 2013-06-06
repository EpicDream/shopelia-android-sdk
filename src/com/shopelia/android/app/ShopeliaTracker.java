package com.shopelia.android.app;

import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.app.tracking.MixPanelTracker;

public interface ShopeliaTracker {

    public int MIXPANEL = 0x1;

    public void init(Context context);

    public void track(String eventName);

    public void track(String eventName, JSONObject object);

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

}
