package com.shopelia.android.http;

import java.io.IOException;

import com.shopelia.android.http.HttpGetPoller.HttpGetRequest;
import com.shopelia.android.http.HttpGetPoller.HttpGetResponse;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

/**
 * A poller polling an Http resource.
 * 
 * @author Pierre Pollastri
 */
public class HttpGetPoller extends AbstractPoller<HttpGetRequest, HttpGetResponse> {

    public static class HttpGetResponse {
        public final HttpResponse response;
        public final Exception exception;

        public HttpGetResponse(HttpResponse response, Exception exception) {
            this.response = response;
            this.exception = exception;
        }

    }

    public static class HttpGetRequest {
        public final String endpoint;
        public final ParameterMap params;

        public HttpGetRequest(String endpoint, ParameterMap params) {
            this.endpoint = endpoint;
            this.params = params;
        }
    }

    public static final String NAME = "HttpGetPoller";

    private ShopeliaRestClient mClient;

    public HttpGetPoller(ShopeliaRestClient client) {
        super(NAME);
        mClient = client;
    }

    @Override
    protected HttpGetResponse execute(HttpGetRequest param) {
        HttpResponse response = null;
        Exception exception = null;
        try {
            response = mClient.get(param.endpoint, param.params);
        } catch (Exception e) {
            exception = e;
        }
        if (response == null) {
            exception = new IOException();
        }
        return new HttpGetResponse(response, exception);
    }
}
