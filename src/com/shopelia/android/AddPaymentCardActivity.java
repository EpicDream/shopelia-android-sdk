package com.shopelia.android;

import io.card.payment.CardIOActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.shopelia.android.app.HostActivity;
import com.shopelia.android.config.Config;

public class AddPaymentCardActivity extends HostActivity {

    private EditText mCardNumberField;
    private EditText mCvvField;
    private EditText mExpiryField;

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_form_payment_card);

        findViewById(R.id.scan).setOnClickListener(mOnScanClickListener);

        mCardNumberField = (EditText) findViewById(R.id.card_numer);
        mCvvField = (EditText) findViewById(R.id.card_numer);
        mExpiryField = (EditText) findViewById(R.id.expiry_date);

    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

    private OnClickListener mOnScanClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent scanIntent = new Intent(AddPaymentCardActivity.this, CardIOActivity.class);

            scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN, Config.CARDIO_TOKEN);

            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true);
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_ZIP, false);
            scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true);
            startActivityForResult(scanIntent, 12);
        }
    };

}