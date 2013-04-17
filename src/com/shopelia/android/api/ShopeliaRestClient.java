package com.shopelia.android.api;

import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.http.HttpMethod;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

/**
 * Default class used to call Shopelia API's methods.
 * 
 * @author Pierre Pollastri
 */
public final class ShopeliaRestClient {

    public static final String LOG_TAG = "ShopelisRestClient";

    private static final String ROOT = "http://zola.epicdream.fr:4444";
    public final static String API_KEY = "52953f1868a7545011d979a8c1d0acbc310dcb5a262981bd1a75c1c6f071ffb4";

    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int READ_TINEOUT = 100000;
    private static final int MAX_RETRIES = 1;

    private static final AndroidHttpClient sHttpClient;

    static {
        sHttpClient = new AndroidHttpClient(ROOT);

        /*
         * Build shopelia HTTP header
         */

        sHttpClient.addHeader("Content-Type", "application/json");
        sHttpClient.addHeader("Accept", "application/json");
        sHttpClient.addHeader("Accept", "application/vnd.shopelia.v1");
        sHttpClient.addHeader("X-Shopelia-ApiKey", API_KEY);

        /*
         * Timeouts and retries
         */

        sHttpClient.setConnectionTimeout(CONNECTION_TIMEOUT);
        sHttpClient.setReadTimeout(READ_TINEOUT);
        sHttpClient.setMaxRetries(MAX_RETRIES);

    }

    private ShopeliaRestClient() {

    }

    /**
     * Convenience method creates a new ParameterMap to hold query params
     * 
     * @return ParameterMap
     */
    public ParameterMap newParams() {
        return sHttpClient.newParams();
    }

    /**
     * Execute a GET request and return the response. The supplied parameters
     * are URL encoded and sent as the query string.
     * 
     * @param path
     * @param params
     * @return
     */
    public static HttpResponse get(Context context, String path, ParameterMap params) {
        return request(context, HttpMethod.GET, path, params, null, null);
    }

    /**
     * Execute a GET request and invoke the callback on completion. The supplied
     * parameters are URL encoded and sent as the query string.
     * 
     * @param path
     * @param params
     * @param callback
     */
    public static void get(Context context, String path, ParameterMap params, AsyncCallback callback) {
        request(context, HttpMethod.GET, path, params, null, callback);
    }

    /**
     * Execute a POST request with parameter map and return the response.
     * 
     * @param path
     * @param params
     */
    public static HttpResponse post(Context context, String path, ParameterMap params) {
        return request(context, HttpMethod.POST, path, params, null, null);
    }

    /**
     * Execute a GET request and invoke the callback on completion. The supplied
     * parameters are URL encoded and sent as the query string.
     * 
     * @param path
     * @param params
     * @param callback
     */
    public static void get(Context context, String path, JSONObject json, AsyncCallback callback) {
        request(context, HttpMethod.POST, path, null, json, callback);
    }

    /**
     * Execute a POST request with parameter map and return the response.
     * 
     * @param path
     * @param params
     */
    public static HttpResponse post(Context context, String path, JSONObject json) {
        return request(context, HttpMethod.POST, path, null, json, null);
    }

    /**
     * Execute a POST request with parameter map and invoke the callback on
     * completion.
     * 
     * @param path
     * @param params
     * @param callback
     */
    public static void post(Context context, String path, ParameterMap params, AsyncCallback callback) {
        request(context, HttpMethod.POST, path, params, null, callback);
    }

    private static HttpResponse request(Context context, HttpMethod method, String path, ParameterMap map, JSONObject object,
            AsyncCallback callback) {
        // sHttpClient.addHeader("X-Shopelia-AuthToken",
        // UserManager.get(context));
        if (callback != null) {
            method.request(sHttpClient, path, map, object, callback);
        } else {
            return method.request(sHttpClient, path, map, object);
        }
        return null;
    }
}
