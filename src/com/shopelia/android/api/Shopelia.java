package com.shopelia.android.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.shopelia.android.StartActivity;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.Tax;
import com.shopelia.android.utils.Vendor;

/**
 * This class ease the use of the Shopelia service and should be the only one used by developers.
 * @author Pierre Pollastri
 *
 */
public final class Shopelia {

	 /**
     * Url of the product to purchase
     */
    public static final String EXTRA_PRODUCT_URL = StartActivity.EXTRA_PRODUCT_URL;

    /**
     * A resource ID or {@link Uri} representing the image of product to
     * purchase
     */
    public static final String EXTRA_PRODUCT_IMAGE = StartActivity.EXTRA_PRODUCT_IMAGE;

    /**
     * Title of the product to purchase
     */
    public static final String EXTRA_PRODUCT_TITLE = StartActivity.EXTRA_PRODUCT_TITLE;

    /**
     * Description of the product to purchase
     */
    public static final String EXTRA_PRODUCT_DESCRIPTION = StartActivity.EXTRA_PRODUCT_DESCRIPTION;

    /**
     * The {@link Vendor} of the product to purchase
     */
    public static final String EXTRA_VENDOR = StartActivity.EXTRA_VENDOR;

    /**
     * The price of the product to purchase
     */
    public static final String EXTRA_PRICE = StartActivity.EXTRA_PRICE;

    /**
     * The shipping fees of the product to purchase
     */
    public static final String EXTRA_SHIPMENT_FEES = StartActivity.EXTRA_SHIPMENT_FEES;

    /**
     * A {@link Tax} object
     */
    public static final String EXTRA_TAX = StartActivity.EXTRA_TAX;

    /**
     * The {@link Currency} of the price
     */
    public static final String EXTRA_CURRENCY = StartActivity.EXTRA_CURRENCY;
	
	private Shopelia() {
		
	}
	
	/**
	 * Checkouts the product of the given url
	 * @param productUrl The product URL
	 * @param data Extra data or null if unnecessary
	 */
	public static final void checkout(Context context, String productUrl, Intent data) {
		if (data == null) {
			data = new Intent();
		}
		data.setClass(context, StartActivity.class);
		context.startActivity(data);
	}
	
}
