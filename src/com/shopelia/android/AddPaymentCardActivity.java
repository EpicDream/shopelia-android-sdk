package com.shopelia.android;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.shopelia.android.algorithm.Luhn;
import com.shopelia.android.app.HostActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.pretty.CardNumberFormattingTextWatcher;
import com.shopelia.android.pretty.DateFormattingTextWatcher;

public class AddPaymentCardActivity extends HostActivity {

    public static final String EXTRA_PAYMENT_CARD = Config.EXTRA_PREFIX + "PAYMENT_CARD";

    private static final int MAX_CARD_VALIDITY_YEAR = 15;
    private static final int MAX_MONTH_VALUE = 12;

    private static final int REQUEST_SCAN_CARD = 16;

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
        initUi(saveState == null ? getIntent().getExtras() : saveState);
    }

    private void initUi(Bundle bundle) {
        if (bundle != null) {
            PaymentCard card = bundle.getParcelable(EXTRA_PAYMENT_CARD);
            if (card != null) {
                mCardNumberField.setText(card.number);
                mCvvField.setText(card.cvv);
                mExpiryField.setText(TextUtils.isEmpty(card.expMonth) || TextUtils.isEmpty(card.expYear) ? "" : card.expMonth + "/"
                        + card.expYear);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        PaymentCard card = new PaymentCard();
        card.number = mCardNumberField.getText().toString().replace(" ", "");
        card.cvv = mCvvField.getText().toString();
        String date = mExpiryField.getText().toString();
        if (date.length() == 5) {
            // card.expMonth = date.substring(0, 2);
            card.expYear = date.substring(3);
            card.expYear = date.substring(3);
        }
        outState.putParcelable(EXTRA_PAYMENT_CARD, card);
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
        }

        isValid = checkIfCardNumberIsMod10(card.number);

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

    private boolean checkIfCardNumberIsMod10(String cardNumber) {
        if (cardNumber.length() < 16 || TextUtils.isDigitsOnly(cardNumber)) {
            // Fire error
            return false;
        }

        if (!Luhn.isValid(cardNumber)) {
            // Fire error
            return false;
        }

        return true;
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
            startActivityForResult(scanIntent, REQUEST_SCAN_CARD);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int result, Intent data) {
        switch (requestCode) {
            case REQUEST_SCAN_CARD:
                if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                    CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                    mCardNumberField.setText(scanResult.cardNumber);
                    Log.d(null, scanResult.cardNumber);
                    mCvvField.setText(scanResult.cvv);
                    mExpiryField.setText(String.format("%02d/%02d", scanResult.expiryMonth, scanResult.expiryYear % 100));
                }
                break;
        }
    };

    private OnClickListener mOnValidateClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            validate();
        }
    };

}
