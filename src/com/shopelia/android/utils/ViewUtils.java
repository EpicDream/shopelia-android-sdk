package com.shopelia.android.utils;

import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.TextView;

/**
 * A sets of utility method in order to extends {@link View} functionalities
 * and/or ease common/complex operations on {@link View}.
 * 
 * @author Pierre Pollastri
 */
public final class ViewUtils {

    /**
     * Interface definition for a callback to be invoked when a view is
     * measured.
     * 
     * @author Pierre Pollastri
     */
    public interface OnMeasureListener {
        public void onMeasure(View v, int width, int height);
    }

    private ViewUtils() {

    }

    /**
     * Force the focus request by programmatically "clicking" on the view
     * 
     * @param v
     */
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

    /**
     * Measure asynchronously a view with the specified width and height. At the
     * end of the measure a {@link OnMeasureListener} callback will be invoked.
     * This method is obviously useful is you want to test a dynamic size like
     * WRAP_CONTENT or MATCH_PARENT. <b>Note:</b> If the view has no parent or
     * no layout parameters an {@link UnsupportedOperationException} will be
     * thrown.
     * 
     * @param v The view to measure
     * @param width The width to apply to the view
     * @param height The height to apply to the view
     * @param l The listener to invoke when the view is measured
     * @throws UnsupportedOperationException Only if the view has no parent or
     *             not layout parameters
     */
    public static void measure(final View v, int width, int height, final OnMeasureListener l) throws UnsupportedOperationException {
        final int initialWidth = v.getWidth();
        final int initialHeight = v.getHeight();
        if (v.getLayoutParams() == null || v.getParent() == null) {
            throw new UnsupportedOperationException("View must be added to a ViewGroup and must have layout parameters");
        }
        v.getLayoutParams().width = width;
        v.getLayoutParams().height = height;
        v.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                v.getViewTreeObserver().removeOnPreDrawListener(this);
                if (l != null) {
                    l.onMeasure(v, v.getWidth(), v.getHeight());
                }
                v.getLayoutParams().width = initialWidth;
                v.getLayoutParams().height = initialHeight;
                v.requestLayout();
                return false;
            }
        });
        v.requestLayout();
    }

    /**
     * Returns theoretical bounds of a {@link TextView}, if it would have the
     * given text and is wrapping content.
     * 
     * @param v
     * @param text
     * @return
     */
    public static Rect getTextViewBounds(TextView v, CharSequence text) {
        Rect out = new Rect();
        TextPaint p = v.getPaint();
        if (text != null) {
            p.getTextBounds(text.toString(), 0, text.length(), out);
        }
        out.bottom += v.getPaddingBottom() + v.getPaddingTop();
        out.right += v.getPaddingLeft() + v.getPaddingRight();
        return out;
    }
}
