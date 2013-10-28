package com.shopelia.android.remote.api;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.shopelia.android.app.tracking.UUIDManager;
import com.shopelia.android.app.tracking.UUIDManager.OnReceiveUuidListener;
import com.shopelia.android.config.Config;
import com.shopelia.android.http.LogcatRequestLogger;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.utils.ContextUtils;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.android.AndroidHttpClient;

/**
 * Default class used to call Shopelia API's methods.
 * 
 * @author Pierre Pollastri
 */
@SuppressLint("NewApi")
public final class ShopeliaRestClient extends AndroidHttpClient {

	public static final String LOG_TAG = "ShopelisRestClient";

	private static final String ROOT = "https://www.shopelia.com:443";
	public final static String API_KEY = "shopelia-sdk-api-key";

	public static final String CONTENT_TYPE_JSON = "application/json";

	private static final int CONNECTION_TIMEOUT = 3000;
	private static final int READ_TIMEOUT = 100000;
	private static final int MAX_RETRIES = 1;

	private interface API {
		String V1 = "application/vnd.shopelia.v1";
		String V2 = "application/vnd.shopelia.v2";
	}

	public final static ShopeliaRestClient V1(Context context) {
		return new ShopeliaRestClient(context, API.V1);
	}

	public final static ShopeliaRestClient V2(Context context) {
		return new ShopeliaRestClient(context, API.V2);
	}

	private ShopeliaRestClient(Context context, String version) {
		super(ROOT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
				&& AndroidHttpClient.getCookieManager() != null) {
			AndroidHttpClient.getCookieManager().setCookiePolicy(
					CookiePolicy.ACCEPT_NONE);
		}

		/*
		 * Build shopelia HTTP header
		 */

		String apiKey = ContextUtils.getMetadataString(context, API_KEY, null);

		if (TextUtils.isEmpty(apiKey)) {
			throw new IllegalStateException(
					"You must configure a Shopelia API key before using Shopelia SDK");
		}

		addHeader("Content-Type", CONTENT_TYPE_JSON);
		addHeader("Accept", CONTENT_TYPE_JSON);
		addHeader("Accept", version);
		addHeader("X-Shopelia-ApiKey", apiKey);

		// addHeader("User-Agent", String.format("shopelia:%s:%d:%s:%s:%s:%s",
		// ShopeliaBuild.SDK, ShopeliaBuild.VERSION.SDK_INT,
		// ShopeliaBuild.VERSION.RELEASE, Build.VERSION.RELEASE,
		// Build.MANUFACTURER + " " + Build.MODEL,
		// UUIDManager.obtainUuid()));

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

		setRequestLogger(new LogcatRequestLogger(LOG_TAG,
				Config.INFO_LOGS_ENABLED));
		authenticate(context);
	}

	public void authenticate(Context context) {
		if (UserManager.get(context).isLogged()) {
			addHeader("X-Shopelia-AuthToken", UserManager.get(context)
					.getAuthToken());
		}
	}

	public void post(final String path, final JSONObject object,
			final AsyncCallback callback) {
		UUIDManager.obtainUuid(new OnReceiveUuidListener() {

			@Override
			public void onReceiveUuid(String uuid) {
				if (object != null) {
					try {
						object.put("visitor", uuid);
					} catch (JSONException e) {
						// Ignore
					}
				}
				post(path, CONTENT_TYPE_JSON, object.toString().getBytes(),
						callback);
			}
		});
	}

	public HttpResponse post(String path, JSONObject object) {
		return post(path, CONTENT_TYPE_JSON, object.toString().getBytes());
	}

	public void put(String path, JSONObject object, AsyncCallback callback) {
		put(path, CONTENT_TYPE_JSON, object.toString().getBytes(), callback);
	}

	public HttpResponse put(String path, JSONObject object) {
		return put(path, CONTENT_TYPE_JSON, object.toString().getBytes());
	}

}
