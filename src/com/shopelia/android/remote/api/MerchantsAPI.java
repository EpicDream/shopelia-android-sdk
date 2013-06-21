package com.shopelia.android.remote.api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.shopelia.android.config.Config;
import com.shopelia.android.model.Merchant;
import com.shopelia.android.utils.JsonUtils;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

/**
 * The Merchant API works in both synchronous and asynchronous way.
 * 
 * @author Pierre Pollastri
 */
public class MerchantsAPI extends ApiHandler {

    private static final String PRIVATE_PREFERENCE = "Shopelia$MerchantAPI.PrivatePreference";
    private static final String PREFS_MERCHANTS = "merchant:merchants";
    private static final String PREFS_LAST_UPDATE = "merchant:last_update";

    private static final long DELAY_BEFORE_UPDATE = 1 * 60 * 60 * 1000;

    private SharedPreferences mPreferences;
    private ArrayList<Merchant> mMerchants;

    public MerchantsAPI(Context context, Callback callback) {
        super(context, callback);
        mPreferences = context.getSharedPreferences(PRIVATE_PREFERENCE, Context.MODE_PRIVATE);
        loadMerchantsFromCache();
    }

    /**
     * Get merchant for a specific url
     * 
     * @param url
     * @return Returns the merchant if the API has the merchant in its cache and
     *         null if it can be retrieve later
     */
    public Merchant getMerchant(final String url) {
        Merchant out = findMerchantByUrl(url);
        if (out == null && canFetchMerchants()) {
            ShopeliaRestClient.get(Command.V1.Merchants.$, null, new AsyncCallback() {

                @Override
                public void onComplete(HttpResponse httpResponse) {
                    try {
                        mMerchants = Merchant.inflate(new JSONArray(httpResponse.getBodyAsString()));
                        saveMerchants(mMerchants);
                        Merchant out = findMerchantByUrl(url);
                        if (out != null && hasCallback()) {
                            getCallback().onRetrieveMerchant(out);
                        }
                    } catch (JSONException e) {
                        if (Config.ERROR_LOGS_ENABLED) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        return out;
    }

    public void update() {
        if (canFetchMerchants()) {
            ShopeliaRestClient.get(Command.V1.Merchants.$, ShopeliaRestClient.newParams(), new AsyncCallback() {

                @Override
                public void onComplete(HttpResponse httpResponse) {
                    if (httpResponse == null) {
                        return;
                    }
                    try {
                        mMerchants = Merchant.inflate(new JSONArray(httpResponse.getBodyAsString()));
                        saveMerchants(mMerchants);
                        if (hasCallback()) {
                            getCallback().onRetrieveMerchants(mMerchants);
                        }
                    } catch (Exception e) {
                        if (Config.ERROR_LOGS_ENABLED) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    super.onError(e);
                    if (Config.ERROR_LOGS_ENABLED) {
                        e.printStackTrace();
                    }
                }

            });
        } else if (hasCallback()) {
            getCallback().onRetrieveMerchants(mMerchants);
        }
    }

    private void loadMerchantsFromCache() {
        String json = mPreferences.getString(PREFS_MERCHANTS, null);
        if (!TextUtils.isEmpty(json)) {
            try {
                mMerchants = Merchant.inflate(new JSONArray(json));
            } catch (Exception e) {
                mMerchants = new ArrayList<Merchant>();
            }
        }
    }

    private boolean canFetchMerchants() {
        return mPreferences.getLong(PREFS_LAST_UPDATE, 0) + DELAY_BEFORE_UPDATE < System.currentTimeMillis();
    }

    private void saveMerchants(List<Merchant> merchants) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREFS_MERCHANTS, JsonUtils.toJson(mMerchants).toString());
        editor.putLong(PREFS_LAST_UPDATE, System.currentTimeMillis());
        editor.commit();
    }

    private Merchant findMerchantByUrl(String url) {
        if (url == null) {
            return null;
        }
        for (Merchant merchant : mMerchants) {
            if (url.contains(merchant.uri.getHost().replace("www.", ""))) {
                return merchant;
            }
        }
        return null;
    }

}
