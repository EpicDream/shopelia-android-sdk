package com.shopelia.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by pollas_p on 21/11/2013.
 */
public class ViewPager extends android.support.v4.view.ViewPager {

    private GestureDetector mGestureDetector;

    public ViewPager(Context context) {
        super(context);
        mGestureDetector = new GestureDetector(context, new Detector());
    }

    public ViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new Detector());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    class Detector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            requestDisallowInterceptTouchEvent(true);

            return false;
        }
    }

}
