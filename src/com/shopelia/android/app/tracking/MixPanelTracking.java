package com.shopelia.android.app.tracking;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.app.ShopeliaTracking;
import com.shopelia.android.utils.JsonUtils;

public class MixPanelTracking implements ShopeliaTracking {

    private static final String MIXPANEL_API_TOKEN = "4938aa680803589593ce9287c33abf43";

    private MixpanelAPI mMixpanelInstance;

    public MixPanelTracking() {

    }

    public void init(Context context) {
        mMixpanelInstance = MixpanelAPI.getInstance(context, MIXPANEL_API_TOKEN);
    }

    public void track(String eventName) {
        track(eventName, null);
    }

    public void track(String eventName, JSONObject object) {
        if (mMixpanelInstance != null) {
            try {
                object = JsonUtils.insert(object, Analytics.Properties.EVENT_TIME, System.currentTimeMillis() / 1000);
            } catch (JSONException e) {

            }
            mMixpanelInstance.track(eventName, object);
        }
    }

    public void flush() {
        if (mMixpanelInstance != null) {
            mMixpanelInstance.flush();
        }
    }

}
