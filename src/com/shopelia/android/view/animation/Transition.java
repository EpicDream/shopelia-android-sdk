package com.shopelia.android.view.animation;

import android.view.View;

public abstract class Transition {

    public interface OnTransitionListener {
        public void onTransitionHalfWay(View in, View out);

        public void onTransitionDone(View in, View out);

        public void onTransitionStart(View in, View out);
    }

    public static class OnTransitionListenerAdapter implements OnTransitionListener {

        @Override
        public void onTransitionHalfWay(View in, View out) {

        }

        @Override
        public void onTransitionDone(View in, View out) {

        }

        @Override
        public void onTransitionStart(View in, View out) {

        }

    }

    private View mTargetIn, mTargetOut;
    private long mDuration;
    private OnTransitionListener mOnTransitionListener;

    public Transition(View in, View out) {
        mTargetIn = in;
        mTargetOut = out;
    }

    public Transition setOnTransitionListener(OnTransitionListener l) {
        mOnTransitionListener = l;
        return this;
    }

    public Transition setDuration(long duration) {
        mDuration = duration;
        return this;
    }

    public long getDuration() {
        return mDuration;
    }

    protected View getIn() {
        return mTargetIn;
    }

    protected View getOut() {
        return mTargetOut;
    }

    public abstract void start();

    protected void onTransitionHalfWay() {
        if (mOnTransitionListener != null) {
            mOnTransitionListener.onTransitionHalfWay(mTargetIn, mTargetOut);
        }
    }

    protected void onTransitionDone() {
        if (mOnTransitionListener != null) {
            mOnTransitionListener.onTransitionDone(mTargetIn, mTargetOut);
        }
    }

    protected void onTransitionStart() {
        if (mOnTransitionListener != null) {
            mOnTransitionListener.onTransitionStart(mTargetIn, mTargetOut);
        }
    }

}
