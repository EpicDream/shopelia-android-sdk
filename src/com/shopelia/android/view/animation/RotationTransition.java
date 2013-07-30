package com.shopelia.android.view.animation;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

public class RotationTransition extends AnimatorTransition {

    private String mPropertyName;

    public RotationTransition(View in, View out, String propertyName) {
        super(in, out, true);
        mPropertyName = propertyName;
    }

    @Override
    protected Animator onCreateOutAnimator(View inView, View outView) {
        ObjectAnimator out = ObjectAnimator.ofFloat(getOut(), mPropertyName, 0, 90).setDuration(getDuration() / 2);
        return out;
    }

    @Override
    protected Animator onCreateInAnimator(View inView, View outView) {
        ObjectAnimator in = ObjectAnimator.ofFloat(getIn(), mPropertyName, -90, 0).setDuration(getDuration() / 2);
        return in;
    }
}
