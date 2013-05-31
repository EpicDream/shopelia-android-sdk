package com.shopelia.android.remote.api;

import android.content.Context;

import com.shopelia.android.model.Merchant;

/**
 * The Merchant API works in both synchronous and asynchronous way.
 * 
 * @author Pierre Pollastri
 */
public class MerchantsAPI extends ApiHandler {

    public MerchantsAPI(Context context, Callback callback) {
        super(context, callback);
    }

    /**
     * Get merchant for a specific url
     * 
     * @param url
     * @return Returns the merchant if the API has the merchant in its cache and
     *         null if it can be retrieve later
     */
    public Merchant getMerchant(String url) {
        return null;
    }

}
