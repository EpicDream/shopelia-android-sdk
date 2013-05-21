package com.shopelia.android.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;

import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.model.Vendor;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.Tax;

/**
 * This class ease the use of the Shopelia service and should be the only one
 * used by developers.
 * 
 * @author Pierre Pollastri
 */
public final class Shopelia {

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
     * The {@link Vendor} of the product to purchase
     */
    public static final String EXTRA_VENDOR = PrepareOrderActivity.EXTRA_VENDOR;

    /**
     * The price of the product to purchase
     */
    public static final String EXTRA_PRICE = PrepareOrderActivity.EXTRA_PRICE;

    /**
     * The shipping fees of the product to purchase
     */
    public static final String EXTRA_SHIPMENT_FEES = PrepareOrderActivity.EXTRA_SHIPMENT_FEES;

    /**
     * A {@link Tax} object
     */
    public static final String EXTRA_TAX = PrepareOrderActivity.EXTRA_TAX;

    /**
     * The {@link Currency} of the price
     */
    public static final String EXTRA_CURRENCY = PrepareOrderActivity.EXTRA_CURRENCY;

    private Intent mData;

    private Shopelia(String productUrl, Vendor vendor) {
        mData = new Intent();
        mData.putExtra(EXTRA_PRODUCT_URL, productUrl);
        mData.putExtra(EXTRA_VENDOR, (Parcelable) vendor);
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

    public static Shopelia obtain(String productUrl) {
        if (productUrl.contains("amazon.fr") || productUrl.contains("amazon.com")) {
            // FIXME Just for V1 tests and alpha
            return new Shopelia(productUrl, Vendor.AMAZON);
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

}
