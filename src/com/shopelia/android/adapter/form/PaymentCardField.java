package com.shopelia.android.adapter.form;

import io.card.payment.CardIOActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.shopelia.android.AddPaymentCardActivity;
import com.shopelia.android.config.Config;

public class PaymentCardField extends ButtonField {

    public PaymentCardField(Context context, int resId) {
        super(context, resId);
    }

    @Override
    protected void onClick(Button view) {
        Activity activity = (Activity) getContext();
        Intent scanIntent = new Intent(activity, AddPaymentCardActivity.class);

        scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN, Config.CARDIO_TOKEN);

        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_ZIP, false);
        activity.startActivityForResult(scanIntent, 12);
    }

    @Override
    public Object getResult() {
        return null;
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

    }

}
