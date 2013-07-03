package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.AddPaymentCardFragment.OnPaymentCardAddedListener;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.PaymentCard;

public class AddPaymentCardActivity extends ShopeliaActivity implements OnPaymentCardAddedListener {

    public static final String ACTIVITY_NAME = "Add Payment Card";
    /**
     * A boolean stating if a payment card has to be added or not in order to
     * continue the checkout process
     */
    public static final String EXTRA_REQUIRED = Config.EXTRA_PREFIX + "REQUIRED";

    /**
     * The {@link PaymentCard} returned by the activity
     */
    public static final String EXTRA_PAYMENT_CARD = Config.EXTRA_PREFIX + "PAYMENT_CARD";

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
        return false;
    }

    @Override
    public void onPaymentCardAdded(PaymentCard card) {
        Intent data = new Intent();
        data.putExtra(EXTRA_PAYMENT_CARD, card);
        setResult(RESULT_OK, data);
        finish();
    }

}
