package com.shopelia.android.http;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * This class allows creation of polling classes. You must implement
 * {@link AbstractPoller#execute(Object)} that will be called periodically by
 * the poller. The poller is asynchronous and
 * {@link AbstractPoller#execute(Object)} should be executed synchronously.
 * 
 * @author Pierre Pollastri
 * @param <ParamType>
 * @param <ResultType>
 * @param <ErrorType>
 */
public abstract class AbstractPoller<ParamType, ResultType> {

    public interface OnPollerEventListener<ResultType> {

        public void onTimeExpired();

        public void onResult(ResultType previousResult, ResultType newResult);

    }

    public static final long DEFAULT_FREQUENCY = 500;
    public static final long NO_EXPIRATION = -1;

    private static final int INFINITE = -1;

    public static final int STATE_STOPPED = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_POLLING = 3;

    private static final int MESSAGE_POLL = 1;
    private static final int MESSAGE_STOP = 2;

    private String mPollerName;
    private ResultType mResult;
    private OnPollerEventListener<ResultType> mOnPollerEventListener;
    private long mFrequency = DEFAULT_FREQUENCY;
    private long mExpiryTime = NO_EXPIRATION;
    private AtomicInteger mInnerState = new AtomicInteger(STATE_STOPPED);
    private PollerThread mPollerThread;
    private PollerHandler mPollerHandler;
    private AtomicReference<ParamType> mParam = new AtomicReference<ParamType>();
    private int mIteration = 0;
    private int mMaxIteration;

    public AbstractPoller(String name) {
        mPollerName = name;
    }

    /**
     * Returns the last successful result
     * 
     * @return
     */
    public ResultType getLastResult() {
        return mResult;
    }

    /**
     * Start polling. This function is reentrant, if the poller is stopped it
     * will begin a new polling session. If the poller is paused it will
     * continue the current session. If the poller is polling it will ignore
     * this call.
     */
    public synchronized void poll() {
        Log.d(null, "ON POLL");
        if (isPaused()) {
            setState(STATE_POLLING);
            sendMessageToPollerThread();
        } else if (isStopped()) {
            mIteration = 0;
            setState(STATE_POLLING);
            if (mExpiryTime == NO_EXPIRATION) {
                mMaxIteration = INFINITE;
            } else {
                mMaxIteration = (int) (mExpiryTime / mFrequency);
            }
            mPollerThread = new PollerThread();
            mPollerThread.start();
        }
    }

    public AbstractPoller<ParamType, ResultType> setOnPollerEventListener(OnPollerEventListener<ResultType> l) {
        mOnPollerEventListener = l;
        return this;
    }

    /**
     * Stop the poller.
     */
    public synchronized void stop() {
        if (!isStopped()) {
            setState(STATE_STOPPED);
        }
    }

    /**
     * Pause the poller it can be resumed by calling
     * {@link AbstractPoller#poll()}
     */
    public synchronized void pause() {
        if (!isPaused()) {
            setState(STATE_PAUSED);
        }
    }

    public AbstractPoller<ParamType, ResultType> setParam(ParamType param) {
        mParam.set(param);
        return this;
    }

    public AbstractPoller<ParamType, ResultType> setRequestFrequency(long frequency) {
        mFrequency = frequency;
        return this;
    }

    public AbstractPoller<ParamType, ResultType> setExpiryDuration(long duration) {
        mExpiryTime = duration;
        return this;
    }

    public boolean isPolling() {
        return mInnerState.get() == STATE_POLLING;
    }

    public boolean isPaused() {
        return mInnerState.get() == STATE_PAUSED;
    }

    public boolean isStopped() {
        return mInnerState.get() == STATE_STOPPED;
    }

    protected void setState(int state) {
        mInnerState.set(state);
    }

    protected int getState() {
        return mInnerState.get();
    }

    private void sendMessageToPollerThread() {
        if (mPollerHandler != null && isPolling()) {
            Log.d(null, "POLL SEND OK");
            Message msg = mPollerHandler.obtainMessage(MESSAGE_POLL, mParam.get());
            mPollerHandler.sendMessageDelayed(msg, mFrequency);
        }
    }

    /**
     * Execute the polling request. If result is null, the request is considered
     * as failed.
     * 
     * @param param
     * @return
     */
    protected abstract ResultType execute(ParamType param);

    private class PollerThread extends HandlerThread {

        public PollerThread() {
            super(mPollerName);
        }

        @Override
        public synchronized void start() {
            super.start();
            mPollerHandler = new PollerHandler(getLooper());
            sendMessageToPollerThread();
            Log.d(null, "POLL SEND TO POLLER THREAD");
        }

    }

    private class PollerHandler extends Handler {

        public PollerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_STOP:
                    getLooper().quit();
                    break;
                case MESSAGE_POLL:
                    Log.d(null, "POLLING");
                    ResultType result = execute((ParamType) msg.obj);
                    Message message = mHandler.obtainMessage();
                    message.what = MESSAGE_POLL;
                    message.obj = result;
                    message.sendToTarget();
                    Log.d(null, "POLLING SEND TO MAIN THREAD");
                    break;
            }
        }

    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_POLL:
                    Log.d(null, "POLL RECEIVED ON MAIN THREAD");
                    if (msg.obj != null) {
                        ResultType old = mResult;
                        mResult = (ResultType) msg.obj;
                        if (mOnPollerEventListener != null && mResult != null) {
                            Log.d(null, "POLL NOTIFY RESULT");
                            mOnPollerEventListener.onResult(old, mResult);
                        }
                    }
                    mIteration++;
                    if ((mMaxIteration == INFINITE || mIteration < mMaxIteration) && isPolling()) {
                        Log.d(null, "POLL RELAUNCH");
                        sendMessageToPollerThread();
                    } else if (mMaxIteration != INFINITE && mIteration >= mMaxIteration && mOnPollerEventListener != null) {
                        Log.d(null, "POLL EXPIRED");
                        mOnPollerEventListener.onTimeExpired();
                    }
                    break;

                default:
                    break;
            }
        }

    };

}
