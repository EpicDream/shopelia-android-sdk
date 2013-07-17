package com.shopelia.android.instrumentation;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class ShopeliaInteractionTracker {

    private static ShopeliaInteractionTracker sInstance;

    private ShopeliaInteractionTracker() {

    }

    public static ShopeliaInteractionTracker getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ShopeliaInteractionTracker();
        }
        sInstance.attach(context);
        return sInstance;
    }

    public void attach(Context context) {

    }

    public void flush() {

    }

    public void track(MotionEvent event) {

    }

    public void track(KeyEvent event) {

    }

}
