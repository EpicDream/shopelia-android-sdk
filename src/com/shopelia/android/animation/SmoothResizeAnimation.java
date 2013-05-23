package com.shopelia.android.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class SmoothResizeAnimation extends Animation {

    private int mFinalWidth;
    private int mInitialWidth;
    private int mFinalHeight;
    private int mInitialHeight;
    private View mVictim;

    public SmoothResizeAnimation(View victim, int initialWidth, int initialHeight, int finalWidht, int finalHeight) {
        mFinalHeight = finalHeight;
        mFinalWidth = finalWidht;
        mInitialHeight = initialHeight;
        mInitialWidth = initialWidth;
        mVictim = victim;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        final int width = mFinalWidth - mInitialWidth;
        final int height = mFinalHeight - mInitialHeight;
        if (mVictim.getLayoutParams() != null) {
            mVictim.getLayoutParams().width = (int) (mInitialWidth + width * interpolatedTime);
            mVictim.getLayoutParams().height = (int) (mInitialHeight + height * interpolatedTime);
        }
        mVictim.requestLayout();
    }

}
