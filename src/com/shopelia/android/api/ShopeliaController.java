package com.shopelia.android.api;

import android.content.Context;

import com.shopelia.android.remote.api.ApiController.OnApiErrorEvent;
import com.shopelia.android.remote.api.MerchantsAPI;
import com.shopelia.android.remote.api.MerchantsAPI.OnRetrieveMerchantEvent;

class ShopeliaController {

    private static ShopeliaController sInstance;

    public static ShopeliaController getInstance() {
        return sInstance != null ? sInstance : (sInstance = new ShopeliaController());
    }

    private ShopeliaController() {

    }

    public void fetch(final Context context, final Shopelia instance) {
        MerchantsAPI api = new MerchantsAPI(context);
        api.register(new Object() {
            @SuppressWarnings("unused")
            public void onEventMainThread(OnRetrieveMerchantEvent event) {
                instance.setStatus(Shopelia.STATUS_AVAILABLE);
            }

            @SuppressWarnings("unused")
            public void onEventMainThread(OnApiErrorEvent event) {
                instance.setStatus(Shopelia.STATUS_NOT_AVAILABLE);
            }

        });
        api.getMerchant(instance.getProductUrl());
    }
}
