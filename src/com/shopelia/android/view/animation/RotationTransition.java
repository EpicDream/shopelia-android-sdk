package com.shopelia.android.view.animation;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class RotationTransition extends Transition {

    private String mPropertyName;

    public RotationTransition(View in, View out, String propertyName) {
        super(in, out);
        mPropertyName = propertyName;
    }

    @Override
    public void start() {
        ObjectAnimator out = ObjectAnimator.ofFloat(getOut(), mPropertyName, 0, 90);
        out.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                onTransitionHalfWay();
                getOut().setVisibility(View.INVISIBLE);
                getIn().setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
        ObjectAnimator in = ObjectAnimator.ofFloat(getIn(), mPropertyName, -90, 0);
        in.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                onTransitionDone();
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                // TODO Auto-generated method stub

            }
        });
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(out, in);
        set.setDuration(getDuration() / 2);
        set.start();
        onTransitionStart();
    }
}
