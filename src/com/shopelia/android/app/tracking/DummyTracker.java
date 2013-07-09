package com.shopelia.android.app.tracking;

import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.app.ShopeliaTracker;

public class DummyTracker implements ShopeliaTracker {

    @Override
    public void init(Context context) {

    }

    @Override
    public void track(String eventName) {

    }

    @Override
    public void track(String eventName, JSONObject object) {

    }

    @Override
    public void onDisplay(String activityName) {

    }

    @Override
    public void onFocusIn(String fieldName) {

    }

    @Override
    public void onValidate(String fieldName) {

    }

    @Override
    public void flush() {

    }

}
