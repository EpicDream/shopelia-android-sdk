package com.shopelia.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * Created by pollas_p on 22/11/2013.
 */
public class OrientationAwareView extends ViewGroup implements SensorEventListener {



    public OrientationAwareView(Context context) {
        super(context);
    }

    public OrientationAwareView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OrientationAwareView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onLayout(boolean b, int left, int top, int right, int bottom) {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

}
