package com.shopelia.android.remote.api;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import android.annotation.SuppressLint;
import android.os.Build;

import com.shopelia.android.config.Config;
import com.shopelia.android.http.LogcatRequestLogger;
import com.turbomanage.httpclient.android.AndroidHttpClient;

/**
 * Default class used to call Shopelia API's methods.
 * 
 * @author Pierre Pollastri
 */
@SuppressLint("NewApi")
public abstract class ShopeliaBaseRestClient extends AndroidHttpClient {

    public static final String LOG_TAG = "ShopelisRestClient";

    private static final String ROOT = "https://www.shopelia.fr:443";
    // private static final String ROOT = "http://zola.epicdream.fr:4444";
    public final static String API_KEY = "52953f1868a7545011d979a8c1d0acbc310dcb5a262981bd1a75c1c6f071ffb4";

    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 100000;
    private static final int MAX_RETRIES = 1;

    static {
        reset();
    }

    private ShopeliaBaseRestClient() {

    }

    public void reset() {

        clearHeaders();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && AndroidHttpClient.getCookieManager() != null) {
            AndroidHttpClient.getCookieManager().setCookiePolicy(CookiePolicy.ACCEPT_NONE);
        }

        /*
         * Build shopelia HTTP header
         */

        addHeader("Content-Type", "application/json");
        addHeader("Accept", "application/json");
        addHeader("Accept", "application/vnd.shopelia.v1");
        addHeader("X-Shopelia-ApiKey", API_KEY);

        /*
         * Timeouts and retries
         */

        setConnectionTimeout(CONNECTION_TIMEOUT);
        setReadTimeout(READ_TIMEOUT);
        setMaxRetries(MAX_RETRIES);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            CookieManager cm = new CookieManager();
            cm.setCookiePolicy(CookiePolicy.ACCEPT_NONE);
            CookieHandler.setDefault(cm);
        }

        /*
         * Logger
         */

        setRequestLogger(new LogcatRequestLogger(LOG_TAG, Config.INFO_LOGS_ENABLED));
    }

}
