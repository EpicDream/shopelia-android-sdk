package com.shopelia.android.graphics;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

public class ColorDrawableAnimation extends DrawableAnimation {

    private final int mColor;

    private int mStartColor;
    private int mCurrentColor;
    private int mEndColor;

    public ColorDrawableAnimation(Drawable target, int color) {
        super(target);
        mColor = color;
        mStartColor = mColor;
    }

    public void start(int startColor, int endColor) {
        stop();
        mStartColor = startColor;
        mEndColor = endColor;
        start();
    }

    public void start(int endColor) {
        stop();
        start(mColor, endColor);
    }

    public void rollback() {
        stop();
        start(mCurrentColor, mColor);
    }

    @Override
    protected void onAnimationStart() {
        mCurrentColor = mStartColor;
    }

    @Override
    protected void onAnimationEnd() {
        mCurrentColor = mEndColor;
        getDrawable().setColorFilter(new ColorizeColorFilter(mColor, mCurrentColor));
    }

    @Override
    protected void onApplyAnimation(float interpolatedTime) {
        mCurrentColor = Color.rgb(compute(interpolatedTime, Color.red(mStartColor), Color.red(mEndColor)), // red
                compute(interpolatedTime, Color.green(mStartColor), Color.green(mEndColor)), // green
                compute(interpolatedTime, Color.blue(mStartColor), Color.blue(mEndColor))); // blue
        getDrawable().setColorFilter(new ColorizeColorFilter(mColor, mCurrentColor));
    }

    private static int compute(float interPolatedTime, int initialValue, int finalValue) {
        return (int) (initialValue + interPolatedTime * (finalValue - initialValue));
    }

}
