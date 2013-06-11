package com.shopelia.android.view.animation;

import java.util.EventListener;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ResizeAnimation extends Animation {

    public interface OnViewRectComputedListener extends EventListener {

        public void onViewRectComputed(View victim, Rect from, Rect to);

    }

    private View mVictim;
    private Rect mFrom = new Rect();
    private Rect mTo = new Rect();

    public ResizeAnimation(View victim, int toWidth, int toHeight) {
        mVictim = victim;
        mFrom.set(0, 0, victim.getWidth(), victim.getHeight());
        mTo.set(0, 0, toWidth, toHeight);
    }

    public void computeSize(final OnViewRectComputedListener l) {
        mVictim.getLayoutParams().width = mTo.width();
        mVictim.getLayoutParams().height = mTo.height();
        mVictim.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                mVictim.getViewTreeObserver().removeOnPreDrawListener(this);
                mTo.set(0, 0, mVictim.getWidth(), mVictim.getHeight());
                if (l != null) {
                    l.onViewRectComputed(mVictim, mFrom, mTo);
                }
                mVictim.getLayoutParams().width = mFrom.width();
                mVictim.getLayoutParams().height = mFrom.height();
                mVictim.requestLayout();
                return false;
            }
        });
        mVictim.requestLayout();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        mVictim.getLayoutParams().width = (int) (interpolatedTime * (mTo.width() - mFrom.width()) + mFrom.width());
        mVictim.getLayoutParams().height = (int) (interpolatedTime * (mTo.height() - mFrom.height()) + mFrom.height());
        mVictim.requestLayout();
    }
}
