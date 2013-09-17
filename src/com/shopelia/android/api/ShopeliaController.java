package com.shopelia.android.api;

import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.model.Merchant;
import com.shopelia.android.remote.api.ApiHandler;
import com.shopelia.android.remote.api.MerchantsAPI;
import com.turbomanage.httpclient.HttpResponse;

class ShopeliaController {

    private static ShopeliaController sInstance;

    public static ShopeliaController getInstance() {
        return sInstance != null ? sInstance : (sInstance = new ShopeliaController());
    }

    private ShopeliaController() {

    }

    public void fetch(final Context context, final Shopelia instance) {
        ApiHandler.CallbackAdapter callback = new ApiHandler.CallbackAdapter() {
            @Override
            public void onRetrieveMerchant(Merchant merchant) {
                super.onRetrieveMerchant(merchant);
                instance.setMerchant(merchant);
                instance.setStatus(Shopelia.STATUS_AVAILABLE);
            }

            @Override
            public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
                super.onError(step, httpResponse, response, e);
                instance.setStatus(Shopelia.STATUS_NOT_AVAILABLE);
            }

        };
        MerchantsAPI api = new MerchantsAPI(context, callback);
        Merchant out = api.getMerchant(instance.getProductUrl());
        if (out != null) {
            callback.onRetrieveMerchant(out);
        }
    }
}
