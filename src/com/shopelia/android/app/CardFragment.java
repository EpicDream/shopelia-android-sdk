package com.shopelia.android.app;

import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.shopelia.android.app.CardChoregrapher.Transaction;

public class CardFragment extends ShopeliaFragment<Void> {

    private Transaction mTransaction;

    public void onViewCreated(android.view.View view, android.os.Bundle savedInstanceState) {
        onBindView(view, savedInstanceState);
        if (mTransaction != null) {
            view.setVisibility(View.GONE);
            mTransaction.notifyViewCreated();
            onCardShouldAppear(mTransaction);
            mTransaction = null;
        } else {
            onCardShouldBeVisible();
        }
    }

    public void attachTransaction(Transaction transaction) {
        mTransaction = transaction;
    }

    public void onCardShouldAppear(final Transaction transaction) {
        getView().setVisibility(View.GONE);
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator anim) {
                getView().setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator anim) {

            }

            @Override
            public void onAnimationEnd(Animator anim) {
                transaction.endTransaction();
            }

            @Override
            public void onAnimationCancel(Animator anim) {

            }
        });
        set.playTogether(ObjectAnimator.ofFloat(getView(), "translationY", 200, 0), ObjectAnimator.ofFloat(getView(), "rotationX", 20, 0),
                ObjectAnimator.ofFloat(getView(), "alpha", 0.f, 1.f));
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(500).start();
    }

    public void onBindView(View view, Bundle savedInstanceState) {

    }

    public void onCardShouldBeVisible() {
        getView().setVisibility(View.VISIBLE);
    }

    public void onCardShouldDisappear(final Transaction transaction) {
        transaction.endTransaction();
    }

}
