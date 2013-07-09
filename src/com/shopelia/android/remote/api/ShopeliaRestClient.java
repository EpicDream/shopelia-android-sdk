package com.shopelia.android.remote.api;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import com.shopelia.android.config.Config;
import com.shopelia.android.http.LogcatRequestLogger;
import com.shopelia.android.manager.UserManager;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

/**
 * Default class used to call Shopelia API's methods.
 * 
 * @author Pierre Pollastri
 */
@SuppressLint("NewApi")
public final class ShopeliaRestClient {

    public static final String LOG_TAG = "ShopelisRestClient";

    private static final String ROOT = "https://www.shopelia.fr:443";
    // private static final String ROOT = "http://zola.epicdream.fr:4444";
    public final static String API_KEY = "52953f1868a7545011d979a8c1d0acbc310dcb5a262981bd1a75c1c6f071ffb4";

    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 100000;
    private static final int MAX_RETRIES = 1;

    private static AndroidHttpClient sHttpClient;

    static {
        reset();
    }

    private ShopeliaRestClient() {

    }

    /**
     * Convenience method creates a new ParameterMap to hold query params
     * 
     * @return ParameterMap
     */
    public static ParameterMap newParams() {
        return sHttpClient.newParams();
    }

    public static void authenticate(Context context) {
        prepare();
        if (UserManager.get(context).isLogged()) {
            sHttpClient.addHeader("X-Shopelia-AuthToken", UserManager.get(context).getAuthToken());
        } else {

        }
    }

    /**
     * Execute a GET request and return the response. The supplied parameters
     * are URL encoded and sent as the query string.
     * 
     * @param path
     * @param params
     * @return
     */
    public static HttpResponse get(String path, ParameterMap params) {
        prepare();
        HttpResponse response = sHttpClient.get(path, params);
        release();
        return response;
    }

    /**
     * Execute a GET request and invoke the callback on completion. The supplied
     * parameters are URL encoded and sent as the query string.
     * 
     * @param path
     * @param params
     * @param callback
     */
    public static void get(String path, ParameterMap params, AsyncCallback callback) {
        prepare();
        sHttpClient.get(path, params, callback);
        release();
    }

    /**
     * Execute a POST request with parameter map and return the response.
     * 
     * @param path
     * @param params
     */
    public static HttpResponse post(String path, ParameterMap params) {
        prepare();
        HttpResponse r = sHttpClient.post(path, params);
        release();
        return r;
    }

    /**
     * Execute a POST request with parameter map and invoke the callback on
     * completion.
     * 
     * @param path
     * @param params
     * @param callback
     */
    public static void post(String path, ParameterMap params, AsyncCallback callback) {
        prepare();
        sHttpClient.post(path, params, callback);
        release();
    }

    /**
     * Execute a POST request with parameter map and return the response.
     * 
     * @param path
     * @param params
     */
    public static HttpResponse post(String path, JSONObject json) {
        prepare();
        HttpResponse r = sHttpClient.post(path, "application/json", json.toString().getBytes());
        release();
        return r;
    }

    /**
     * Execute a POST request with parameter map and invoke the callback on
     * completion.
     * 
     * @param path
     * @param params
     * @param callback
     */
    public static void post(String path, JSONObject object, AsyncCallback callback) {
        prepare();
        sHttpClient.post(path, "application/json", object.toString().getBytes(), callback);
        release();
    }

    /**
     * Execute a POST request with parameter map and return the response.
     * 
     * @param path
     * @param params
     */
    public static HttpResponse post(String path, byte[] object) {
        prepare();
        HttpResponse r = sHttpClient.post(path, "application/json", object);
        release();
        return r;
    }

    /**
     * Execute a POST request with parameter map and invoke the callback on
     * completion.
     * 
     * @param path
     * @param params
     * @param callback
     */
    public static void post(String path, byte[] object, AsyncCallback callback) {
        prepare();
        sHttpClient.post(path, "application/json", object, callback);
        release();
    }

    public static void put(String path, JSONObject object, AsyncCallback callback) {
        prepare();
        sHttpClient.put(path, "application/json", object.toString().getBytes(), callback);
        release();
    }

    /**
     * Execute a DELETE request and invoke the callback on completion. The
     * supplied parameters are URL encoded and sent as the query string.
     * 
     * @param path
     * @param params
     * @param callback
     */
    public static void delete(String path, ParameterMap params, AsyncCallback callback) {
        prepare();
        sHttpClient.delete(path, params, callback);
        release();
    }

    public static final void reset() {

        sHttpClient = new AndroidHttpClient(ROOT);
        sHttpClient.clearHeaders();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && AndroidHttpClient.getCookieManager() != null) {
            AndroidHttpClient.getCookieManager().setCookiePolicy(CookiePolicy.ACCEPT_NONE);
        }

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
        sHttpClient.setReadTimeout(READ_TIMEOUT);
        sHttpClient.setMaxRetries(MAX_RETRIES);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            CookieManager cm = new CookieManager();
            cm.setCookiePolicy(CookiePolicy.ACCEPT_NONE);
            CookieHandler.setDefault(cm);
        }

        /*
         * Logger
         */

        sHttpClient.setRequestLogger(new LogcatRequestLogger(LOG_TAG, Config.INFO_LOGS_ENABLED));
    }

    private static final void prepare() {
        if (sHttpClient == null) {
            reset();
        }
    }

    private static final void release() {
        if (sHttpClient != null) {
            sHttpClient = null;
        }
    }

}
