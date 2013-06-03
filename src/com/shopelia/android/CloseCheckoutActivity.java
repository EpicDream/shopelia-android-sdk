package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;

import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.analytics.AnalyticsBuilder;
import com.shopelia.android.app.ShopeliaActivity;

public class CloseCheckoutActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "Thank you";

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_close_checkout_activity);
        if (saveState == null) {
            track(Analytics.Events.Steps.Finalize.BEGIN);
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
        track(Analytics.Events.Steps.Finalize.END,
                AnalyticsBuilder.prepareMethodPackage(this, Analytics.Properties.Steps.Finalizing.Method.BACK_ON_APPLICATION));
    }

}
