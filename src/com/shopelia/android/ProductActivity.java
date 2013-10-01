package com.shopelia.android;

import android.os.Bundle;

import com.shopelia.android.app.CardHolderActivity;
import com.shopelia.android.config.Config;

public class ProductActivity extends CardHolderActivity {

    /**
     * Url of the product to purchase
     */
    public static final String EXTRA_PRODUCT_URL = Config.EXTRA_PREFIX + "PRODUCT_URL";

    public static final String ACTIVITY_NAME = "Product";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This is the activity to be called by the SDK. It must init the order
        // for the rest of the Workflow
        getIntent().putExtra(EXTRA_INIT_ORDER, true);
        setActivityStyle(STYLE_FULLSCREEN);
        super.onCreate(savedInstanceState);
        addCard(new ProductSummaryCardFragment(), 0, false, ProductSummaryCardFragment.TAG);
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

}
