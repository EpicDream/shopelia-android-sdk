package com.shopelia.android.view.animation;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

public class CrossFadingTransition extends AnimatorTransition {

    public CrossFadingTransition(View in, View out, boolean crossFade) {
        super(in, out, crossFade);
    }

    @Override
    protected Animator onCreateInAnimator(View in, View out) {
        return ObjectAnimator.ofFloat(in, "alpha", 0.f, 1.f);
    }

    @Override
    protected Animator onCreateOutAnimator(View in, View out) {
        return ObjectAnimator.ofFloat(out, "alpha", 1.f, 0.f);
    }

}
