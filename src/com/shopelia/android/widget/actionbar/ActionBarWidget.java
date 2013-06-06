package com.shopelia.android.widget.actionbar;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.shopelia.android.R;

public class ActionBarWidget extends FrameLayout {

    private ViewGroup mOptionsContainer;
    private ViewGroup mBufferContainer;

    public ActionBarWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        addView(inflater.inflate(R.layout.shopelia_action_bar, this, false));
        mOptionsContainer = (ViewGroup) findViewById(R.id.title_options_container_first);
        mBufferContainer = (ViewGroup) findViewById(R.id.title_options_container_second);
        swapBuffer();
    }

    public ViewGroup getOptionsContainer() {
        return mOptionsContainer;
    }

    public ViewGroup getBufferContainer() {
        return mBufferContainer;
    }

    public void swapBuffer() {
        ViewGroup tmp = mOptionsContainer;
        mOptionsContainer = mBufferContainer;
        mBufferContainer = tmp;
        mBufferContainer.setVisibility(INVISIBLE);
        mOptionsContainer.setVisibility(VISIBLE);
    }

    public void swap(Transition transition) {
        if (transition == null) {
            throw new NullPointerException("Transition cannot be null at this point");
        }
        transition.start(this);
    }

    public static abstract class Transition {

        private static final long INVALID_DURATION = 0;

        private ActionBarWidget mView;
        private long mDuration = INVALID_DURATION;
        private long mStartTime = INVALID_DURATION;
        private long framerate = 1000 / 40;
        private Interpolator mInterpolator = new LinearInterpolator();
        private Handler mHandler;

        public void start(ActionBarWidget actionBarWidget) {
            mView = actionBarWidget;
            if (mDuration == INVALID_DURATION) {
                mDuration = getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime);
            }
            mStartTime = INVALID_DURATION;
            onTransitionStart();
            mAnimationRunnable.run();
        }

        public void setDuration(long duration) {
            mDuration = duration;
        }

        public long getDuration() {
            return mDuration;
        }

        public void setInterpolator(Interpolator interpolator) {
            mInterpolator = interpolator;
            if (mInterpolator == null) {
                mInterpolator = new LinearInterpolator();
            }
        }

        public Interpolator getInterpolator() {
            return mInterpolator;
        }

        public void onTransitionStart() {

        }

        public void onTransitionEnd() {

        }

        public abstract void apply(long currentTime);

        public ActionBarWidget getView() {
            return mView;
        }

        public Context getContext() {
            return mView.getContext();
        }

        private Runnable mAnimationRunnable = new Runnable() {

            @Override
            public void run() {
                if (mStartTime == INVALID_DURATION) {
                    mStartTime = System.currentTimeMillis();
                }
                long currentTime = System.currentTimeMillis() - mStartTime;
                apply(currentTime);
                if (currentTime >= getDuration()) {
                    apply(getDuration());
                    onTransitionEnd();
                } else {
                    mHandler.postDelayed(this, framerate);
                }
            }
        };

    }

}
