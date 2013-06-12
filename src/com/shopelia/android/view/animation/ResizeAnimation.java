package com.shopelia.android.view.animation;

import java.util.EventListener;

import android.graphics.Rect;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.shopelia.android.utils.ViewUtils;
import com.shopelia.android.utils.ViewUtils.OnMeasureListener;

public class ResizeAnimation extends Animation {

    public interface OnViewRectComputedListener extends EventListener {

        public void onViewRectComputed(View victim, Rect from, Rect to);

    }

    private View mTargetView;
    private Rect mFrom = new Rect();
    private Rect mTo = new Rect();

    public ResizeAnimation(View target, int toWidth, int toHeight) {
        mTargetView = target;
        mFrom.set(0, 0, target.getWidth(), target.getHeight());
        mTo.set(0, 0, toWidth, toHeight);
    }

    public void computeSize(final OnViewRectComputedListener l) {
        ViewUtils.measure(mTargetView, mTo.width(), mTo.height(), new OnMeasureListener() {

            @Override
            public void onMeasure(View v, int width, int height) {
                mTo.set(0, 0, width, height);
                if (l != null) {
                    l.onViewRectComputed(v, mFrom, mTo);
                }
            }
        });
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        mTargetView.getLayoutParams().width = (int) (interpolatedTime * (mTo.width() - mFrom.width()) + mFrom.width());
        mTargetView.getLayoutParams().height = (int) (interpolatedTime * (mTo.height() - mFrom.height()) + mFrom.height());
        mTargetView.requestLayout();
    }
}
