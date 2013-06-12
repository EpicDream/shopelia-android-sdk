package com.shopelia.android.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.shopelia.android.config.Config;
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
        if (!Config.INFO_LOGS_ENABLED) {
            return;
        }
        log("=== HTTP Request ===");
        log(urlConnection.getRequestMethod() + " " + urlConnection.getURL().toString());
        if (content instanceof String) {
            log("Content: " + (String) content);
        }
        logHeaders(urlConnection.getRequestProperties());
    }

    /**
     * Iterate over request or response headers and log them.
     * 
     * @param map
     */
    private void logHeaders(Map<String, List<String>> map) {
        if (map != null) {
            for (String field : map.keySet()) {
                if (field != null) {
                    List<String> headers = map.get(field);
                    for (String header : headers) {
                        log(field + ":" + header);
                    }
                }
            }
        }
    }

    @Override
    public void logResponse(HttpResponse res) {
        if (!Config.INFO_LOGS_ENABLED) {
            return;
        }
        if (res != null) {
            log("=== HTTP Response ===");
            log("Receive url: " + res.getUrl());
            log("Status: " + res.getStatus());
            log("Content:\n" + res.getBodyAsString());
        }
    }

}
