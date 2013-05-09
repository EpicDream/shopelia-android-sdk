package com.shopelia.android.http;

import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.turbomanage.httpclient.HttpResponse;

public class HttpPoller extends HandlerThread {

    private static final String THREAD_NAME = "HttpPoller";
    private static final long DEFAULT_POLLING_FREQUENCY = 200;

    public static final int MESSAGE_POLL = 0x1;
    public static final int STATE_STOPPED = 0;
    public static final int STATE_PAUSED = 1;
    public static final int STATE_POLLING = 2;

    private PollerHandler mPollerHandler;
    private String mRequest;
    private Handler mHandler;

    private final AtomicInteger mState = new AtomicInteger(STATE_STOPPED);

    public HttpPoller() {
        super(THREAD_NAME, Thread.NORM_PRIORITY);
    }

    public synchronized void poll(String request) {
        if (isPaused() && mRequest != null && mRequest.equals(request)) {
            resumePolling();
            return;
        } else {
            mRequest = request;
            mState.set(STATE_POLLING);
            sendPollingRequest(mRequest);
        }
    }

    public void setPollerCallback(Handler handler) {
        mHandler = handler;
    }

    public synchronized void end() {
        mState.set(STATE_STOPPED);
        mRequest = null;
        mPollerHandler.removeMessages(MESSAGE_POLL);
    }

    public synchronized void pause() {
        mState.set(STATE_PAUSED);
        mPollerHandler.removeMessages(MESSAGE_POLL);
    }

    public synchronized void resumePolling() {
        if (mRequest != null && isPaused()) {
            mState.set(STATE_POLLING);
            sendPollingRequest(mRequest);
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
        mRequest = null;
        mPollerHandler = new PollerHandler(getLooper());
    }

    private void sendPollingRequest(String request) {
        Message msg = mPollerHandler.obtainMessage();
        msg.what = MESSAGE_POLL;
        msg.obj = request;
        mPollerHandler.sendMessageDelayed(msg, DEFAULT_POLLING_FREQUENCY);
    }

    private class PollerHandler extends Handler {

        @SuppressLint("HandlerLeak")
        public PollerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Handler handler = mHandler;
            switch (msg.what) {
                case MESSAGE_POLL:
                    HttpResponse response = null;
                    Exception exception = null;
                    try {
                        Thread.sleep(DEFAULT_POLLING_FREQUENCY / 2);
                        response = ShopeliaRestClient.get((String) msg.obj, null);
                    } catch (Exception e) {
                        exception = e;
                    }
                    if (handler != null) {
                        Message message = handler.obtainMessage();
                        message.what = MESSAGE_POLL;
                        message.obj = response != null ? response : exception;
                        handler.sendMessage(message);
                    }
                    if (isPolling() && mRequest !=null) {
                        sendPollingRequest(mRequest);
                    }
                    break;

                default:
                    break;
            }
        }

    }

    public static abstract class PollerCalback extends Handler {

        protected abstract void onComplete(HttpResponse response);

        protected void onError(Exception e) {

        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_POLL:
                    if (msg.obj instanceof HttpResponse) {
                        onComplete((HttpResponse) msg.obj);
                    } else {
                        onError((Exception) msg.obj);
                    }
                    break;

                default:
                    break;
            }
        }

    }

}
