package com.shopelia.android;

import android.os.Bundle;

import com.shopelia.android.app.ShopeliaActivity;

public class RecoverPincodeActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "RecoverPincode";

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
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
