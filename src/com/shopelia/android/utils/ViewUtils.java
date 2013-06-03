package com.shopelia.android.utils;

import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

public final class ViewUtils {

    private ViewUtils() {

    }

    public static void forceRequestFocus(final View v) {
        (new Handler()).postDelayed(new Runnable() {

            public void run() {
                MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0,
                        0, 0);
                v.dispatchTouchEvent(event);
                event.recycle();
                event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0);
                v.dispatchTouchEvent(event);
                event.recycle();
            }
        }, 400);
    }
}
