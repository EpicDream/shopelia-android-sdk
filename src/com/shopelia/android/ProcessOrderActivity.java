package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.app.ShopeliaActivity;

public class ProcessOrderActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "Order Summary";

    public static final int REQUEST_AUTHENTICATE = 0x16;

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_process_order_activity);

        if (saveState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, new ConfirmationFragment());
            ft.commit();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return true;
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

}
