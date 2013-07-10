package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.app.ShopeliaActivity;

public class CloseCheckoutActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "Thank you";

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_close_checkout_activity);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new CloseCheckoutFragment());
        ft.commit();
        if (saveState == null) {
            getTracker().track(Analytics.Events.Steps.ORDER_COMPLETED);
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
