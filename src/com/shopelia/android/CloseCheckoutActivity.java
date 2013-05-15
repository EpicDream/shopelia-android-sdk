package com.shopelia.android;

import android.os.Bundle;

import com.shopelia.android.app.HostActivity;

public class CloseCheckoutActivity extends HostActivity {

    public static final String ACTIVITY_NAME = "CloseCheckout";

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_close_checkout_activity);
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

}
