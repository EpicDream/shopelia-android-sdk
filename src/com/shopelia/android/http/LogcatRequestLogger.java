package com.shopelia.android.http;

import java.io.IOException;
import java.net.HttpURLConnection;

import android.util.Log;

import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.RequestLogger;

public class LogcatRequestLogger implements RequestLogger {

    private boolean mIsLoggingEnabled;
    private String mLogTag;

    public LogcatRequestLogger(String logTag, boolean isEnabled) {
        mIsLoggingEnabled = isEnabled;
        mLogTag = logTag;
    }

    @Override
    public boolean isLoggingEnabled() {
        return mIsLoggingEnabled;
    }

    @Override
    public void log(String msg) {
        Log.d(mLogTag, msg);
    }

    @Override
    public void logRequest(HttpURLConnection urlConnection, Object content) throws IOException {
        Log.d(mLogTag, urlConnection.getRequestMethod() + " " + urlConnection.getURL().toString());
        if (content instanceof String) {
            Log.d(mLogTag, "Content: " + (String) content);
        } else if (content instanceof byte[]) {
            Log.d(mLogTag, "Content: " + new String((byte[]) content));
        }
    }

    @Override
    public void logResponse(HttpResponse httpResponse) {

    }

}
