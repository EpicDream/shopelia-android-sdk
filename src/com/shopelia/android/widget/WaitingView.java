package com.shopelia.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * A special view drawing a loader with evolution displayed in percent. Note
 * that this view is only here to help the user waiting for Shopelia taking care
 * of the checkout process and the evolution of the progress is purely empiric.
 * 
 * @author Pierre Pollastri
 */
public class WaitingView extends View {

    private static final int MAX_ANGLE = 360;
    private static final long FRAMERATE = 40;
    private static final long FREQUENCY = 1000L / FRAMERATE;

    private long mExpectedTime = 10000L;

    private LinearInterpolator mInterpolator;

    private Handler mHandler = new Handler();

    // Variables
    private long mCurrentTime = 0L;

    // Draw properties
    private float mProgressThickness = 40;
    private RectF mOval = new RectF();

    // Render objects
    private Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mCenterCircleBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public WaitingView(Context context) {
        this(context, null);
    }

    public WaitingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaitingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setProgressColor(0xFF2d9f35);
        setBackgroundColor(0xFFd2d2d2);
        setCenterCircleBackgroundColor(0xFFe5e5e5);
    }

    public void start() {
        mInterpolator = new LinearInterpolator();
        mHandler.removeCallbacks(mAnimationRunnable);
        mHandler.post(mAnimationRunnable);
    }

    public void stop() {
        mHandler.removeCallbacks(mAnimationRunnable);
    }

    public void setProgressColor(int color) {
        mProgressPaint.setColor(color);
        mProgressPaint.setStyle(Style.FILL);
        invalidate();
    }

    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
        mBackgroundPaint.setStyle(Style.FILL);
        invalidate();
    }

    public void setCenterCircleBackgroundColor(int color) {
        mCenterCircleBackgroundPaint.setColor(color);
        mCenterCircleBackgroundPaint.setStyle(Style.FILL);
        invalidate();
    }

    int angle = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mOval.set(0, 0, getWidth(), getHeight());
        // int angle = (int) (MAX_ANGLE *
        // mInterpolator.getInterpolation(mCurrentTime / mExpectedTime));
        canvas.drawCircle(getWidth() / 2.f, getHeight() / 2.f, getWidth() / 2.f, mBackgroundPaint);
        canvas.drawArc(mOval, -90, angle, true, mProgressPaint);
        canvas.drawCircle(getWidth() / 2.f, getHeight() / 2.f, getWidth() / 2.f - mProgressThickness, mCenterCircleBackgroundPaint);

    }

    private Runnable mAnimationRunnable = new Runnable() {

        @Override
        public void run() {
            mCurrentTime += FREQUENCY;
            angle++;
            invalidate();
            mHandler.postDelayed(this, FREQUENCY);
        }
    };

}
