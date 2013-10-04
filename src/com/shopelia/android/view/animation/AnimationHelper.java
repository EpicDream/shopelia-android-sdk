package com.shopelia.android.view.animation;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.shopelia.android.concurent.ScheduledTask;

import de.greenrobot.event.EventBus;

public final class AnimationHelper {

    private static EventBus sEventBus;

    public interface OnAnimationCompleteListener {
        public void onAnimationComplete();
    }

    private static class OnAnimationCompleteEvent {
        public final View target;

        private OnAnimationCompleteEvent(View target) {
            this.target = target;
        }

    }

    public static class OnVerticalSlideInCompleteEvent extends OnAnimationCompleteEvent {

        public OnVerticalSlideInCompleteEvent(View target) {
            super(target);
        }

    }

    public static class OnVerticalSlideOutCompleteEvent extends OnAnimationCompleteEvent {

        public OnVerticalSlideOutCompleteEvent(View target) {
            super(target);
        }

    }

    public static class OnVerticalLandInCompleteEvent extends OnAnimationCompleteEvent {

        public OnVerticalLandInCompleteEvent(View target) {
            super(target);
        }

    }

    private AnimationHelper() {

    }

    public static EventBus getEventBus() {
        return sEventBus != null ? sEventBus : (sEventBus = new EventBus());
    }

    public static void playVerticalSlideIn(View target, int height, long delay) {
        playVerticalSlideIn(target, height, delay, null);
    }

    public static void playVerticalSlideIn(final View target, final int height, long delay, final OnAnimationCompleteListener l) {
        target.setVisibility(View.GONE);
        new ScheduledTask().schedule(new Runnable() {

            @Override
            public void run() {
                AnimatorSet set = new AnimatorSet();
                set.addListener(new AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator anim) {
                        target.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animator anim) {

                    }

                    @Override
                    public void onAnimationEnd(Animator anim) {
                        if (l != null) {
                            l.onAnimationComplete();
                        }
                        getEventBus().post(new OnVerticalSlideInCompleteEvent(target));
                    }

                    @Override
                    public void onAnimationCancel(Animator anim) {

                    }
                });
                set.playTogether(ObjectAnimator.ofFloat(target, "translationY", height, 0),

                ObjectAnimator.ofFloat(target, "alpha", 0.f, 1.f));
                set.setInterpolator(new DecelerateInterpolator());
                set.setDuration(800).start();
            }
        }, delay);
    }

    public static void playVerticalSlideOut(View target, int height, long delay) {
        playVerticalSlideOut(target, height, delay, null);
    }

    public static void playVerticalSlideOut(final View target, final int height, long delay, final OnAnimationCompleteListener l) {
        new ScheduledTask().schedule(new Runnable() {

            @Override
            public void run() {
                AnimatorSet set = new AnimatorSet();
                set.addListener(new AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator anim) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator anim) {

                    }

                    @Override
                    public void onAnimationEnd(Animator anim) {
                        target.setVisibility(View.GONE);
                        if (l != null) {
                            l.onAnimationComplete();
                        }
                        getEventBus().post(new OnVerticalSlideOutCompleteEvent(target));
                    }

                    @Override
                    public void onAnimationCancel(Animator anim) {

                    }
                });
                set.playTogether(ObjectAnimator.ofFloat(target, "translationY", 0, height),

                ObjectAnimator.ofFloat(target, "alpha", 1.f, 0.f));
                set.setInterpolator(new DecelerateInterpolator());
                set.setDuration(400).start();
            }
        }, delay);
    }

    public static void playVerticalLandIn(final View target, final int height, long delay, final OnAnimationCompleteListener l) {
        target.setVisibility(View.GONE);
        new ScheduledTask().schedule(new Runnable() {

            @Override
            public void run() {
                AnimatorSet set = new AnimatorSet();
                set.addListener(new AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator anim) {
                        target.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animator anim) {

                    }

                    @Override
                    public void onAnimationEnd(Animator anim) {
                        if (l != null) {
                            l.onAnimationComplete();
                        }
                        getEventBus().post(new OnVerticalLandInCompleteEvent(target));
                    }

                    @Override
                    public void onAnimationCancel(Animator anim) {

                    }
                });
                set.playTogether(ObjectAnimator.ofFloat(target, "translationY", height, 0),
                        ObjectAnimator.ofFloat(target, "rotationX", 20, 0), ObjectAnimator.ofFloat(target, "alpha", 0.f, 1.f));
                set.setInterpolator(new DecelerateInterpolator());
                set.setDuration(500).start();
            }
        }, delay);
    }

    public static void playHorizontalSwipeOut(final View target, long delay, final OnAnimationCompleteListener l) {
        target.setVisibility(View.VISIBLE);
        new ScheduledTask().schedule(new Runnable() {

            @Override
            public void run() {
                AnimatorSet set = new AnimatorSet();
                set.addListener(new AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator anim) {
                        target.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animator anim) {

                    }

                    @Override
                    public void onAnimationEnd(Animator anim) {
                        target.post(new Runnable() {

                            @Override
                            public void run() {
                                target.setVisibility(View.GONE);
                                if (l != null) {
                                    l.onAnimationComplete();
                                }
                            }
                        });

                        getEventBus().post(new OnVerticalLandInCompleteEvent(target));
                    }

                    @Override
                    public void onAnimationCancel(Animator anim) {

                    }
                });
                set.playTogether(ObjectAnimator.ofFloat(target, "translationX", 0, target.getWidth()),
                        ObjectAnimator.ofFloat(target, "alpha", 1.f, 0.5f));
                set.setInterpolator(new AccelerateDecelerateInterpolator());
                set.setDuration(400).start();
            }
        }, delay);
    }

}
