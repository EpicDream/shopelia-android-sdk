package com.shopelia.android.view.animation;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;

public abstract class AnimatorTransition extends Transition {

    private Animator mInAnimator;
    private Animator mOutAnimator;
    private boolean mIsSequential = true;

    public AnimatorTransition(View in, View out, boolean playSequentially) {
        super(in, out);
        playSequentially(playSequentially);
    }

    public void playSequentially(boolean value) {
        mIsSequential = value;
    }

    protected abstract Animator onCreateInAnimator(View in, View out);

    protected abstract Animator onCreateOutAnimator(View in, View out);

    @Override
    public void start() {
        mInAnimator = onCreateInAnimator(getIn(), getOut());
        mOutAnimator = onCreateOutAnimator(getIn(), getOut());
        mOutAnimator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                onTransitionStart();
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                onTransitionHalfWay();
                if (mIsSequential) {
                    getOut().setVisibility(View.INVISIBLE);
                    getIn().setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
        mInAnimator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                onTransitionDone();
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
        AnimatorSet set = new AnimatorSet();
        set.setDuration(getDuration() / 2);
        if (mIsSequential) {
            set.playSequentially(mOutAnimator, mInAnimator);
        } else {
            set.playTogether(mOutAnimator, mInAnimator);
        }
        set.start();
    }

}
