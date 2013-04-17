package com.shopelia.android.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.shopelia.android.config.Config;

/**
 * Helper class for creating Shopelia feature intents and using specific
 * options.
 * 
 * @author Pierre Pollastri
 */
public final class ShopeliaHelper {

    /**
     * A {@link String} representing the product url to purchase.
     */
    public static final String EXTRA_PURCHASE_PRODUCT = Config.EXTRA_PREFIX + "PURCHASE_PRODUCT";

    private ShopeliaHelper() {

    }

    public static Bundle preparePurchaseExtras(Bundle extras, String productUrl) {
        if (extras == null) {
            extras = new Bundle();
        }
        extras.putString(EXTRA_PURCHASE_PRODUCT, productUrl);
        return extras;
    }

    public static Intent createPurchaseIntent(Context context, String productUrl) {
        Intent intent = new Intent();
        intent.putExtras(preparePurchaseExtras(null, productUrl));
        return intent;
    }

}
