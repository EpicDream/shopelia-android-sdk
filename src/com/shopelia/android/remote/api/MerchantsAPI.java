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
import com.turbomanage.httpclient.ParameterMap;

/**
 * The Merchant API works in both synchronous and asynchronous way.
 * 
 * @author Pierre Pollastri
 */
public class MerchantsAPI extends ApiController {

    public static class OnRetrieveMerchantEvent extends OnResourceEvent<Merchant> {
        private OnRetrieveMerchantEvent(Merchant resource) {
            super(resource);
        }
    }

    public static class OnRetriveMerchantsEvent {
        public ArrayList<Merchant> resources;

        private OnRetriveMerchantsEvent(ArrayList<Merchant> merchants) {
            resources = merchants;
        }

    }

    private static final String PRIVATE_PREFERENCE = "Shopelia$MerchantAPI.PrivatePreference";
    private static final String PREFS_MERCHANTS = "merchant:merchants";
    private static final String PREFS_LAST_UPDATE = "merchant:last_update";

    private static final long DELAY_BEFORE_UPDATE = 1 * 60 * 60 * 1000;

    private static Class<?>[] sEventTypes = new Class<?>[] {
            OnRetrieveMerchantEvent.class, OnRetriveMerchantsEvent.class
    };

    private SharedPreferences mPreferences;
    private ArrayList<Merchant> mMerchants;

    public MerchantsAPI(Context context) {
        super(context);
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
            ShopeliaRestClient.V1(getContext()).get(Command.V1.Merchants.$, null, new AsyncCallback() {

                @Override
                public void onComplete(HttpResponse httpResponse) {
                    try {
                        mMerchants = Merchant.inflate(new JSONArray(httpResponse.getBodyAsString()));
                        saveMerchants(mMerchants);
                        Merchant out = findMerchantByUrl(url);
                        if (out != null) {
                            getEventBus().post(new OnRetrieveMerchantEvent(out));
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

    public boolean update() {
        if (canFetchMerchants()) {
            ShopeliaRestClient.V1(getContext()).get(Command.V1.Merchants.$, new ParameterMap(), new AsyncCallback() {

                @Override
                public void onComplete(HttpResponse httpResponse) {
                    if (httpResponse == null) {
                        return;
                    }
                    try {
                        mMerchants = Merchant.inflate(new JSONArray(httpResponse.getBodyAsString()));
                        saveMerchants(mMerchants);
                        getEventBus().post(new OnRetriveMerchantsEvent(mMerchants));
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
            return false;
        } else {
            getEventBus().post(new OnRetriveMerchantsEvent(mMerchants));
        }
        return true;
    }

    @Override
    public Class<?>[] getEventTypes() {
        return sEventTypes;
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
        if (url == null || mMerchants == null) {
            return null;
        }
        for (Merchant merchant : mMerchants) {
            if (merchant.isValid() && url.contains(merchant.uri.getHost().replace("www.", ""))) {
                return merchant;
            }
        }
        return null;
    }

}
