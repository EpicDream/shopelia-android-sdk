package com.shopelia.android.app;

import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.app.tracking.MixPanelTracking;

public interface ShopeliaTracking {

    public int MIXPANEL = 0x1;

    public void init(Context context);

    public void track(String eventName);

    public void track(String eventName, JSONObject object);

    public void flush();

    public static class Factory {

        public static ShopeliaTracking create(int provider) {
            switch (provider) {
                case MIXPANEL:
                    return new MixPanelTracking();
                default:
                    return new MixPanelTracking();
            }
        }

    }

}
