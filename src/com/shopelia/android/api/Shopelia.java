package com.shopelia.android.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;

import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.app.ShopeliaTracking;
import com.shopelia.android.model.Merchant;
import com.shopelia.android.remote.api.ApiHandler;
import com.shopelia.android.remote.api.MerchantsAPI;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.Tax;

/**
 * This class ease the use of the Shopelia service and should be the only one
 * used by developers.
 * 
 * @author Pierre Pollastri
 */
public final class Shopelia {

    public interface Callback {
        public void onShopeliaIsAvailable(Shopelia instance);
    }

    /**
     * Url of the product to purchase
     */
    public static final String EXTRA_PRODUCT_URL = PrepareOrderActivity.EXTRA_PRODUCT_URL;

    /**
     * A resource ID or {@link Uri} representing the image of product to
     * purchase
     */
    public static final String EXTRA_PRODUCT_IMAGE = PrepareOrderActivity.EXTRA_PRODUCT_IMAGE;

    /**
     * Title of the product to purchase
     */
    public static final String EXTRA_PRODUCT_TITLE = PrepareOrderActivity.EXTRA_PRODUCT_TITLE;

    /**
     * Description of the product to purchase
     */
    public static final String EXTRA_PRODUCT_DESCRIPTION = PrepareOrderActivity.EXTRA_PRODUCT_DESCRIPTION;

    /**
     * The {@link Merchant} of the product to purchase
     */
    public static final String EXTRA_MERCHANT = PrepareOrderActivity.EXTRA_MERCHANT;

    /**
     * The price of the product to purchase
     */
    public static final String EXTRA_PRICE = PrepareOrderActivity.EXTRA_PRICE;

    /**
     * The shipping fees of the product to purchase
     */
    public static final String EXTRA_SHIPPING_PRICE = PrepareOrderActivity.EXTRA_SHIPPING_PRICE;

    /**
     * The shipping info of the product to purchase
     */
    public static final String EXTRA_SHIPPING_INFO = PrepareOrderActivity.EXTRA_SHIPPING_INFO;

    /**
     * A {@link Tax} object
     */
    public static final String EXTRA_TAX = PrepareOrderActivity.EXTRA_TAX;

    /**
     * The {@link Currency} of the price
     */
    public static final String EXTRA_CURRENCY = PrepareOrderActivity.EXTRA_CURRENCY;

    private Intent mData;

    private Shopelia(Context context, String productUrl, Merchant merchant) {
        mData = new Intent();
        mData.putExtra(EXTRA_PRODUCT_URL, productUrl);
        mData.putExtra(EXTRA_MERCHANT, (Parcelable) merchant);
        ShopeliaTracking tracking = new ShopeliaTracking(context);
        tracking.track(Analytics.Events.UInterface.SHOPELIA_BUTTON_SHOWN);
        tracking.flush();
    }

    /**
     * Checkouts the product of the given url
     * 
     * @param productUrl The product URL
     * @param data Extra data or null if unnecessary
     */
    public static final void checkout(Context context, String productUrl, Intent data) {
        if (data == null) {
            data = new Intent();
        }
        data.setClass(context, PrepareOrderActivity.class);
        if (productUrl != null) {
            data.putExtra(EXTRA_PRODUCT_URL, productUrl);
        }
        context.startActivity(data);
    }

    public void checkout(Context context) {
        checkout(context, null, mData);
    }

    /**
     * Obtains a new instance of {@link Shopelia} only if the merchant of the
     * product url is available on Shopelia. Even if the method returns null, it
     * will check online if the merchant is available and notify you later.
     * 
     * @param context
     * @param productUrl The product url
     * @param callback The {@link Callback} instance used to notify you that the
     *            merchant is available
     * @return
     */
    public static Shopelia obtain(final Context context, final String productUrl, final Callback callback) {
        MerchantsAPI api = new MerchantsAPI(context, new ApiHandler.CallbackAdapter() {
            @Override
            public void onRetrieveMerchant(Merchant merchant) {
                super.onRetrieveMerchant(merchant);
                if (callback != null) {
                    callback.onShopeliaIsAvailable(new Shopelia(context, productUrl, merchant));
                }
            }
        });
        Merchant out = api.getMerchant(productUrl);
        if (out != null) {
            return new Shopelia(context, productUrl, out);
        }
        return null;
    }

    public Shopelia setProductImageUri(Uri imageUri) {
        mData.putExtra(EXTRA_PRODUCT_IMAGE, imageUri);
        return this;
    }

    public Shopelia setProductName(String productName) {
        mData.putExtra(EXTRA_PRODUCT_TITLE, productName);
        return this;
    }

    public Shopelia setProductPrice(float price) {
        mData.putExtra(EXTRA_PRICE, price);
        return this;
    }

    public Shopelia setProductShippingPrice(float price) {
        mData.putExtra(EXTRA_SHIPPING_PRICE, price);
        return this;
    }

    public Shopelia setProductShippingInfo(String info) {
        mData.putExtra(EXTRA_SHIPPING_INFO, info);
        return this;
    }

}
