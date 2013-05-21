package com.shopelia.android;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.app.ShopeliaActivity;

public class ProcessOrderActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "ProcessOrder";

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
    protected boolean isPartOfOrderWorkFlow() {
        return true;
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

}
