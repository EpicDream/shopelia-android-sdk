package com.shopelia.android.graphics;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.shopelia.android.utils.TimeUnits;

public abstract class DrawableAnimation implements Animatable {

    private static final long FRAMERATE = TimeUnits.SECONDS / 30;

    private long mDuration = TimeUnits.SECONDS;
    private long mStartTime;
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private Drawable mDrawable;

    private boolean mIsRunning = false;

    public DrawableAnimation(Drawable drawable) {
        mDrawable = drawable;
    }

    public void setDuration(long duration) {
        if (!mIsRunning) {
            mDuration = duration;
        }
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    @Override
    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void start() {
        if (!mIsRunning) {
            mIsRunning = true;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            onAnimationStart();
            mDrawable.scheduleSelf(mAnimationRunnable, SystemClock.uptimeMillis() + FRAMERATE);
        }
    }

    @Override
    public void stop() {
        if (mIsRunning) {
            mIsRunning = false;
        }
    }

    private Runnable mAnimationRunnable = new Runnable() {

        @Override
        public void run() {
            long now = AnimationUtils.currentAnimationTimeMillis();
            long duration = now - mStartTime;
            if (duration >= mDuration || !mIsRunning) {
                stop();
                onAnimationEnd();
            } else {
                float time = mInterpolator.getInterpolation(duration / (float) mDuration);
                onApplyAnimation(time);
            }
            if (mIsRunning) {
                long frameDuration = AnimationUtils.currentAnimationTimeMillis() - now;
                mDrawable.invalidateSelf();
                if (frameDuration < FRAMERATE) {
                    mDrawable.scheduleSelf(mAnimationRunnable, SystemClock.uptimeMillis() + FRAMERATE);
                } else {
                    mAnimationRunnable.run();
                }
            }

        }

    };

    /**
     * Initialization of variables
     */
    protected abstract void onAnimationStart();

    protected abstract void onAnimationEnd();

    protected abstract void onApplyAnimation(float interpolatedTime);

}
