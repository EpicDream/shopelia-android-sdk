package com.shopelia.android.http;

import org.json.JSONObject;

import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

public enum HttpMethod {
    GET("GET") {
        @Override
        public void request(AndroidHttpClient httpClient, String path, ParameterMap params, JSONObject object, AsyncCallback callback) {
            httpClient.get(path, params, callback);
        }

        @Override
        public HttpResponse request(AndroidHttpClient httpClient, String path, ParameterMap params, JSONObject object) {
            return httpClient.get(path, params);
        }
    },
    POST("POST") {
        @Override
        public void request(AndroidHttpClient httpClient, String path, ParameterMap params, JSONObject object, AsyncCallback callback) {
            if (object == null) {
                httpClient.post(path, params, callback);
            } else {
                httpClient.post(path, "application/json", object.toString().getBytes(), callback);
            }
        }

        @Override
        public HttpResponse request(AndroidHttpClient httpClient, String path, ParameterMap params, JSONObject object) {
            if (object == null) {
                return httpClient.post(path, params);
            } else {
                return httpClient.post(path, "application/json", object.toString().getBytes());
            }
        }
    },
    PUT("PUT") {
        @Override
        public void request(AndroidHttpClient httpClient, String path, ParameterMap params, JSONObject object, AsyncCallback callback) {
            if (object == null) {
                throw new UnsupportedOperationException("PUT request must have JSON body");
            }
            httpClient.post(path, "application/json", object.toString().getBytes(), callback);
        }

        @Override
        public HttpResponse request(AndroidHttpClient httpClient, String path, ParameterMap params, JSONObject object) {
            if (object == null) {
                throw new UnsupportedOperationException("PUT request must have JSON body");
            }
            return httpClient.post(path, "application/json", object.toString().getBytes());
        }
    },
    DELETE("DELETE") {
        @Override
        public void request(AndroidHttpClient httpClient, String path, ParameterMap params, JSONObject object, AsyncCallback callback) {
            httpClient.delete(path, params, callback);
        }

        @Override
        public HttpResponse request(AndroidHttpClient httpClient, String path, ParameterMap params, JSONObject object) {
            return httpClient.delete(path, params);
        }
    };

    private String mName;

    private HttpMethod(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public abstract void request(AndroidHttpClient httpClient, String path, ParameterMap params, JSONObject object, AsyncCallback callback);

    public abstract HttpResponse request(AndroidHttpClient httpClient, String path, ParameterMap params, JSONObject object);

}
