package com.shopelia.android.app;

import org.json.JSONObject;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class ShopeliaTracking {

    private static final String MIXPANEL_API_TOKEN = "4938aa680803589593ce9287c33abf43";

    private MixpanelAPI mMixpanelInstance;

    public ShopeliaTracking() {

    }

    public ShopeliaTracking(Context context) {
        init(context);
    }

    public void init(Context context) {
        mMixpanelInstance = MixpanelAPI.getInstance(context, MIXPANEL_API_TOKEN);
    }

    public void track(String eventName) {
        track(eventName, null);
    }

    public void track(String eventName, JSONObject object) {
        if (mMixpanelInstance != null) {
            mMixpanelInstance.track(eventName, object);
        }
    }

    public void flush() {
        if (mMixpanelInstance != null) {
            mMixpanelInstance.flush();
        }
    }

}
