package com.shopelia.android.concurent;

import java.lang.ref.WeakReference;

import android.os.Handler;

/**
 * A class used to fire a given {@link Runnable} at fixed rate on the calling
 * {@link Thread}. It can do both use fixed rate or single shot mode. This class
 * uses an internal handler to schedule tasks. This can be both provided or lazy
 * instantiated. If no {@link Handler} is provided it will be created when a
 * task is scheduled.
 * 
 * @author Pierre Pollastri
 */
public class ScheduledTask {

    private static final long SINGLE_SHOT = -1L;

    private Handler mHandler;
    private long mPeriod = SINGLE_SHOT;
    private long mDelay;
    private WeakReference<Runnable> mRunnable = new WeakReference<Runnable>(null);

    private Object mToken = new Object();

    public ScheduledTask() {

    }

    /**
     * Use a custom handler
     * 
     * @param handler
     */
    public ScheduledTask(Handler handler) {
        mHandler = handler;
    }

    public void scheduleAtFixedRate(final Runnable runnable, final long delay, final long period) {
        mRunnable = new WeakReference<Runnable>(runnable);
        mPeriod = period;
        mDelay = delay;
        start();
    }

    public void schedule(final Runnable runnable, final long delay) {
        mRunnable = new WeakReference<Runnable>(runnable);
        mPeriod = SINGLE_SHOT;
        mDelay = delay;
        start();
    }

    public void stop() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(mToken);
        }
    }

    private void start() {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        mHandler.postDelayed(mPrivateRunnable, mDelay + (mPeriod != SINGLE_SHOT ? mPeriod : 0));
    }

    private Runnable mPrivateRunnable = new Runnable() {

        @Override
        public void run() {
            mDelay = 0L;
            if (mRunnable.get() != null) {
                mRunnable.get().run();
                if (mPeriod != SINGLE_SHOT) {
                    mHandler.postDelayed(mPrivateRunnable, mPeriod);
                }
            }
        }
    };

}
