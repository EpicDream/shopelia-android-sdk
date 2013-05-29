package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;

import com.shopelia.android.app.ShopeliaActivity;

public class CloseCheckoutActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "Thank you";

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_close_checkout_activity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(RESULT_OK, data);
        finish();
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
