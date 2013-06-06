package com.shopelia.android.app.tracking;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.app.ShopeliaTracker;
import com.shopelia.android.config.Build;
import com.shopelia.android.utils.JsonUtils;

public class MixPanelTracker implements ShopeliaTracker {

    private static final String MIXPANEL_API_TOKEN = "4938aa680803589593ce9287c33abf43";

    private MixpanelAPI mMixpanelInstance;
    private String mSessionId;

    public MixPanelTracker() {

    }

    public void init(Context context) {
        mMixpanelInstance = MixpanelAPI.getInstance(context, MIXPANEL_API_TOKEN);
        if (context instanceof ShopeliaActivity) {
            mSessionId = ((ShopeliaActivity) context).getSessionId();
        }
    }

    public void track(String eventName) {
        track(eventName, null);
    }

    public void track(String eventName, JSONObject object) {
        if (mMixpanelInstance != null) {
            try {
                if (!TextUtils.isEmpty(mSessionId)) {
                    object = JsonUtils.insert(object, Analytics.Properties.SESSION, mSessionId);
                }
                object = JsonUtils.insert(object, Analytics.Properties.SDK_INT, Build.VERSION.SDK_INT);
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
