package com.shopelia.android;

import io.card.payment.CardIOActivity;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.shopelia.android.app.HostActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.pretty.CardNumberFormattingTextWatcher;
import com.shopelia.android.pretty.DateFormattingTextWatcher;

public class AddPaymentCardActivity extends HostActivity {

    private static final int MAX_CARD_VALIDITY_YEAR = 15;
    private static final int MAX_MONTH_VALUE = 12;

    private EditText mCardNumberField;
    private EditText mCvvField;
    private EditText mExpiryField;

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_form_payment_card);

        findViewById(R.id.scan).setOnClickListener(mOnScanClickListener);

        mCardNumberField = (EditText) findViewById(R.id.card_numer);
        mCvvField = (EditText) findViewById(R.id.cvv);
        mExpiryField = (EditText) findViewById(R.id.expiry_date);

        mCardNumberField.setFilters(new InputFilter[] {
            new CardNumberFormattingTextWatcher.CardNumberInputFilter()
        });
        mCardNumberField.addTextChangedListener(new CardNumberFormattingTextWatcher());

        mExpiryField.addTextChangedListener(new DateFormattingTextWatcher());

        findViewById(R.id.validate).setOnClickListener(mOnValidateClickListener);

    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

    private void validate() {
        boolean isValid = true;
        String date = mExpiryField.getText().toString();
        PaymentCard card = new PaymentCard();
        card.number = mCardNumberField.getText().toString().replace(" ", "");
        card.cvv = mCvvField.getText().toString();

        if (card.cvv.length() != 3 && card.cvv.length() != 4) {
            // Fire error
            isValid = false;
        }
        if (date.length() != 5) {
            // Fire error
            isValid = false;
        } else {
            card.expMonth = date.substring(0, 2);
            card.expYear = date.substring(3);
            isValid = isValid && checkIfExpiryDateIsValid(card.expMonth, card.expYear);
            Log.d(null, "2 IS VALID " + isValid);
        }

        if (isValid) {
            finish();
        } else {

        }

    }

    private boolean checkIfExpiryDateIsValid(String month, String year) {
        boolean isValid = true;
        try {
            int M = Integer.valueOf(month);
            int Y = Integer.valueOf(year);
            Calendar calendar = Calendar.getInstance();

            if (calendar.get(Calendar.YEAR) % 100 == Y && calendar.get(Calendar.MONTH) > M) {
                // Fire error
                isValid = false;
            } else if (Y > calendar.get(Calendar.YEAR) % 100 + MAX_CARD_VALIDITY_YEAR) {
                // Fire error
                isValid = false;
            } else if (M < 1 || M > MAX_MONTH_VALUE) {
                // Fire error
                isValid = false;
            } else if (Y < calendar.get(Calendar.YEAR) % 100) {
                // Fire error
            }

        } catch (NumberFormatException e) {
            isValid = false;
        }
        return isValid;
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

    private OnClickListener mOnValidateClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            validate();
        }
    };

}
