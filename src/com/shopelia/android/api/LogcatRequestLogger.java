package com.shopelia.android.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.RequestLogger;

/**
 * @author Cyril Mottier
 */
public class LogcatRequestLogger implements RequestLogger {

    private static final String LOG_TAG = "LogcatRequestLogger";

    public static final int LOG_REQUEST = 1 << 0;
    public static final int LOG_RESPONSE = 1 << 1;
    public static final int LOG_ALL = LOG_REQUEST | LOG_RESPONSE;

    private int mLogFlags;

    public LogcatRequestLogger(int logFlags) {
        mLogFlags = logFlags;
    }

    @Override
    public boolean isLoggingEnabled() {
        return mLogFlags != 0;
    }

    @Override
    public void log(String msg) {
        Log.i(LOG_TAG, msg);
    }

    @Override
    public void logRequest(HttpURLConnection uc, Object content) throws IOException {
        if ((mLogFlags & LOG_REQUEST) == 0) {
            return;
        }
        log("=== HTTP Request ===");
        log(uc.getRequestMethod() + " " + uc.getURL().toString());
        if (content instanceof String) {
            log("Content: " + (String) content);
        }
        logHeaders(uc.getRequestProperties());
    }

    @Override
    public void logResponse(HttpResponse res) {
        if ((mLogFlags & LOG_RESPONSE) == 0) {
            return;
        }
        if (res != null) {
            log("=== HTTP Response ===");
            log("Receive url: " + res.getUrl());
            log("Status: " + res.getStatus());
            logHeaders(res.getHeaders());
            log("Content:\n" + res.getBodyAsString());
        }
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

}
