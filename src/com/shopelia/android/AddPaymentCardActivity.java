package com.shopelia.android;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.app.ShopeliaActivity;

public class AddPaymentCardActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "Add Payment Card";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.shopelia_add_payment_card_activity);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, new AddPaymentCardFragment());
        ft.commit();
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return super.isPartOfOrderWorkFlow();
    }

}
