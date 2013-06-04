package com.shopelia.android.widget.actionbar.transition;

import android.content.Context;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

import com.shopelia.android.widget.actionbar.ActionBarWidget.Transition;

public class AnimationTransition extends Transition {

    private Context mContext;
    private Animation mInAnimation;
    private Animation mOutAnimation;
    private boolean mIsExecutedSequentially = true;

    private Transformation mInTransformation = new Transformation();
    private Transformation mOutTransformation = new Transformation();

    public AnimationTransition(Context context) {
        mContext = context;
    }

    public void setExecuteAnimationsSequentially(boolean isExecutedSequentialy) {
        mIsExecutedSequentially = isExecutedSequentialy;
    }

    public void setInAnimation(int resId) {
        mInAnimation = AnimationUtils.loadAnimation(mContext, resId);
    }

    public void setOutAnimation(int resId) {
        mOutAnimation = AnimationUtils.loadAnimation(mContext, resId);
    }

    @Override
    public void onTransitionStart() {
        super.onTransitionStart();
        if (mIsExecutedSequentially) {
            setDuration(mInAnimation.computeDurationHint() + mOutAnimation.computeDurationHint());
        } else {
            setDuration(Math.max(mInAnimation.computeDurationHint(), mOutAnimation.computeDurationHint()));
        }
    }

    @Override
    public void apply(long currentTime) {
        if (mIsExecutedSequentially) {

        } else {

        }
    }

    private void applySequentially(long currentTime) {
        ViewGroup victim = currentTime > mOutAnimation.computeDurationHint() ? getView().getBufferContainer() : getView()
                .getOptionsContainer();
        boolean result = currentTime > mOutAnimation.computeDurationHint() ? mInAnimation.getTransformation(currentTime, mInTransformation)
                : mOutAnimation.getTransformation(currentTime, mInTransformation);

    }

    private void applySynchronous(long currentTime) {

    }

}
