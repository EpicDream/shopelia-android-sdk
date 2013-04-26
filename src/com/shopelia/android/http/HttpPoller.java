package com.shopelia.android.http;

import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.turbomanage.httpclient.AsyncCallback;

public class HttpPoller extends HandlerThread {

    private static final String THREAD_NAME = "HttpPoller";
    private static final long DEFAULT_POLLING_FREQUENCY = 500;

    public static final int MESSAGE_POLL = 0x1;
    public static final int STATE_STOPPED = 0;
    public static final int STATE_PAUSED = 1;
    public static final int STATE_POLLING = 2;

    private PollerHandler mPollerHandler;
    private String mRequest;
    private AsyncCallback mAsyncCallback;

    private final AtomicInteger mState = new AtomicInteger(STATE_STOPPED);

    public HttpPoller() {
        super(THREAD_NAME, Thread.NORM_PRIORITY);
    }

    public synchronized void poll(String request) {
        if (isPaused()) {
            resumePolling();
            return;
        } else {
            mRequest = request;
        }
    }

    public synchronized void setAsyncCallback(AsyncCallback callback) {
        mAsyncCallback = callback;
    }

    public synchronized void end() {
        mRequest = null;
        mPollerHandler.removeMessages(MESSAGE_POLL);
    }

    public synchronized void pause() {
        mState.set(STATE_PAUSED);
        mPollerHandler.removeMessages(MESSAGE_POLL);
    }

    public synchronized void resumePolling() {
        if (mRequest != null && isPaused()) {

        }
    }

    public boolean isPolling() {
        return mState.get() == STATE_POLLING;
    }

    public boolean isPaused() {
        return mState.get() == STATE_PAUSED;
    }

    public boolean isStopped() {
        return mState.get() == STATE_STOPPED;
    }

    @Override
    public synchronized void start() {
        super.start();
        mPollerHandler = new PollerHandler(getLooper());
    }

    private class PollerHandler extends Handler {

        public PollerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }

    }

}
