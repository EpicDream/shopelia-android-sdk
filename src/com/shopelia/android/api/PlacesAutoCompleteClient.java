package com.shopelia.android.api;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

import com.shopelia.android.config.Config;
import com.shopelia.android.http.LogcatRequestLogger;
import com.shopelia.android.model.Address;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

public final class PlacesAutoCompleteClient {

    public static final String LOG_TAG = "ShopelisRestClient";

    private static final String ROOT = "https://maps.googleapis.com/maps/api/place/autocomplete/json";

    public static final String API_KEY = "AIzaSyDy_IvoH2PkTUM3e_5FBwMjHotCkNX1fTY";

    public static final String PLACES_TYPES = "geocode";

    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int READ_TINEOUT = 100000;
    private static final int MAX_RETRIES = 1;

    private static final AndroidHttpClient sHttpClient;

    private interface GooglePlacesApi {
        public String INPUT = "input";
        public String SENSOR = "sensor";
        public String OFFSET = "offset";
        public String LOCATION = "location";
        public String RADIUS = "radius";
        public String TYPES = "types";
        public String LANGUAGE = "language";
        public String API_KEY = "key";
    }

    static {
        sHttpClient = new AndroidHttpClient(ROOT);

        /*
         * Timeouts and retries
         */

        sHttpClient.setConnectionTimeout(CONNECTION_TIMEOUT);
        sHttpClient.setReadTimeout(READ_TINEOUT);
        sHttpClient.setMaxRetries(MAX_RETRIES);

        /*
         * Logger
         */

        sHttpClient.setRequestLogger(new LogcatRequestLogger(LOG_TAG, Config.INFO_LOGS_ENABLED));
    }

    private PlacesAutoCompleteClient() {

    }

    public static List<Address> autocomplete(Context context, String input, int cursorOffset) {
        ParameterMap params = new ParameterMap();
        params.add(GooglePlacesApi.INPUT, input);
        params.add(GooglePlacesApi.SENSOR, String.valueOf(true));
        params.add(GooglePlacesApi.OFFSET, String.valueOf(cursorOffset));
        params.add(GooglePlacesApi.API_KEY, API_KEY);
        params.add(GooglePlacesApi.TYPES, PLACES_TYPES);
        params.add(GooglePlacesApi.LANGUAGE, Locale.getDefault().getCountry());
        HttpResponse response = sHttpClient.get("", params);
        Log.d(null, "RESULT = " + response.getBodyAsString());
        return null;
    }
}
