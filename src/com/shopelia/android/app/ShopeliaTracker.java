package com.shopelia.android.app;

import java.lang.ref.SoftReference;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.app.tracking.DummyTracker;
import com.shopelia.android.app.tracking.MixPanelTracker;
import com.shopelia.android.model.User;

public interface ShopeliaTracker {

    public int PROVIDER_MIXPANEL = 0x1;
    public int PROVIDER_DEFAULT = PROVIDER_MIXPANEL;

    public void init(Context context);

    public void identify(User user);

    public void unidentify();

    public void track(String eventName);

    public void track(String eventName, JSONObject object);

    public void onDisplay(String activityName);

    public void onFocusIn(String fieldName);

    public void onValidate(String fieldName);

    public void flush();

    public static class Factory {

        private static SoftReference<ShopeliaTracker> sInstance = new SoftReference<ShopeliaTracker>(null);

        public static ShopeliaTracker create(int provider) {
            switch (provider) {
                case PROVIDER_MIXPANEL:
                    return new MixPanelTracker();
                default:
                    return new DummyTracker();
            }
        }

        public static ShopeliaTracker getDefault(Context context) {
            ShopeliaTracker tracker = sInstance.get();
            if (tracker == null) {
                tracker = create(PROVIDER_DEFAULT);
                tracker.init(context);
                sInstance = new SoftReference<ShopeliaTracker>(tracker);
            }
            return tracker;
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
