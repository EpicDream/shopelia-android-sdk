package com.shopelia.android.app.tracking;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.app.ShopeliaTracker;
import com.shopelia.android.config.Build;
import com.shopelia.android.model.User;

public class MixPanelTracker implements ShopeliaTracker {

    private static final String MIXPANEL_API_TOKEN = "95c15bf80bf7bf93cb1c673865c75a22";

    private static final boolean LOG = false;
    public static final String LOG_TAG = "MixPanelTracker";

    private MixpanelAPI mMixpanelInstance;
    private String mSessionId;

    public interface Api {
        public String EMAIL = "$email";
        public String FIRST_NAME = "$first_name";
        public String LAST_NAME = "$last_name";
    }

    public MixPanelTracker() {

    }

    public void init(Context context) {
        mMixpanelInstance = MixpanelAPI.getInstance(context, MIXPANEL_API_TOKEN);
        if (context instanceof ShopeliaActivity) {
            mSessionId = ((ShopeliaActivity) context).getSessionId();
        }
        mMixpanelInstance.registerSuperProperties(getSuperProperties(context));
    }

    public JSONObject getSuperProperties(Context context) {
        JSONObject properties = new JSONObject();
        try {
            properties.put(Analytics.Properties.SDK, Build.SDK);
            properties.put(Analytics.Properties.SDK_VERSION, Build.VERSION.RELEASE);
            if (!TextUtils.isEmpty(mSessionId)) {
                properties.put(Analytics.Properties.SESSION, mSessionId);
            }
        } catch (JSONException e) {
            // Do nothing
        }
        return properties;
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

    @Override
    public void onDisplay(String activityName) {
        track(Analytics.Events.Activities.DISPLAY + " " + activityName);
        if (LOG) {
            Log.d(LOG_TAG, "DISPLAY " + activityName);
        }
    }

    @Override
    public void onFocusIn(String fieldName) {
        track(Analytics.Events.UserInteractions.FOCUS_IN + " " + fieldName);
        if (LOG) {
            Log.d(LOG_TAG, "FOCUS IN " + fieldName);
        }
    }

    @Override
    public void onValidate(String fieldName) {
        track(Analytics.Events.UserInteractions.OK + " " + fieldName);
        if (LOG) {
            Log.d(LOG_TAG, "VALIDATE " + fieldName);
        }
    }

    @Override
    public void identify(User user) {
        if (mMixpanelInstance != null && user != null) {
            mMixpanelInstance.getPeople().identify(user.email);
            mMixpanelInstance.getPeople().set(Api.FIRST_NAME, user.firstname);
            mMixpanelInstance.getPeople().set(Api.LAST_NAME, user.lastname);
            mMixpanelInstance.getPeople().set(Api.EMAIL, user.email);
        }
    }

    @Override
    public void unidentify() {
        if (mMixpanelInstance != null) {
            mMixpanelInstance.getPeople().deleteUser();
        }
    }

}
