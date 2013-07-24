package com.shopelia.android.app.tracking;

import android.content.Context;

import com.shopelia.android.app.ShopeliaTracker;

public class VikingTracker extends ShopeliaTracker {

    private static VikingTracker sInstance;

    public static VikingTracker getInstance() {
        return sInstance != null ? sInstance : (sInstance = new VikingTracker());
    }

    private VikingTracker() {

    }

    @Override
    public void init(Context context) {

    }

    @Override
    public void flush() {

    }

}
