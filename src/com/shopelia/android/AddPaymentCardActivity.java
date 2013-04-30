package com.shopelia.android;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.shopelia.android.algorithm.Luhn;
import com.shopelia.android.app.HostActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.pretty.CardNumberFormattingTextWatcher;
import com.shopelia.android.pretty.DateFormattingTextWatcher;
import com.shopelia.android.widget.FormEditText;

public class AddPaymentCardActivity extends HostActivity {

    public static final String EXTRA_PAYMENT_CARD = Config.EXTRA_PREFIX + "PAYMENT_CARD";

    private static final int MAX_CARD_VALIDITY_YEAR = 15;
    private static final int MAX_MONTH_VALUE = 12;

    private static final int REQUEST_SCAN_CARD = 16;

    private FormEditText mCardNumberField;
    private FormEditText mCvvField;
    private FormEditText mExpiryField;

    private ImageView mHeaderIcon;
    private TextView mHeaderTitle;

    private PaymentCard mPaymentCard;

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_form_payment_card);

        findViewById(R.id.scan).setOnClickListener(mOnScanClickListener);

        mCardNumberField = (FormEditText) findViewById(R.id.card_numer);
        mCvvField = (FormEditText) findViewById(R.id.cvv);
        mExpiryField = (FormEditText) findViewById(R.id.expiry_date);
        mCardNumberField.setFilters(new InputFilter[] {
            new CardNumberFormattingTextWatcher.CardNumberInputFilter()
        });
        mCardNumberField.addTextChangedListener(new CardNumberFormattingTextWatcher());

        mCardNumberField.setOnFocusChangeListener(mOnFocusChangeListener);
        mCvvField.setOnFocusChangeListener(mOnFocusChangeListener);
        mExpiryField.setOnFocusChangeListener(mOnFocusChangeListener);

        mCardNumberField.addTextChangedListener(mTextWatcher);
        mCvvField.addTextChangedListener(mTextWatcher);
        mExpiryField.addTextChangedListener(mTextWatcher);

        mExpiryField.addTextChangedListener(new DateFormattingTextWatcher());

        View headerFrame = findViewById(R.id.header_frame);
        mHeaderIcon = (ImageView) headerFrame.findViewById(R.id.icon);
        mHeaderTitle = (TextView) headerFrame.findViewById(R.id.title);

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
        mHeaderIcon.setImageResource(R.drawable.shopelia_card);
        mHeaderTitle.setText(R.string.shopelia_form_main_payment_method);
        validate(false);
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

    private boolean validate(boolean fireError) {
        boolean isValid = true;
        PaymentCard card = new PaymentCard();

        isValid = checkCardNumber(card, fireError) && isValid;
        isValid = checkCvv(card, fireError) && isValid;
        isValid = checkExpiryDate(card, fireError) && isValid;

        if (isValid) {
            mHeaderTitle.setTextColor(getResources().getColor(R.color.shopelia_headerTitleSectionOkColor));
            mHeaderIcon.setImageResource(R.drawable.shopelia_check_ok);
            mPaymentCard = card;
        } else {
            mHeaderIcon.setImageResource(R.drawable.shopelia_card);
            mHeaderTitle.setText(R.string.shopelia_form_main_payment_method);
            mPaymentCard = null;
        }

        return isValid;
    }

    private boolean checkCvv(PaymentCard card, boolean fireError) {
        boolean isValid = true;
        String cvv = mCvvField.getText().toString();
        if (TextUtils.isEmpty(cvv)) {
            if (fireError) {
                mCvvField.setError(true);
            }
            isValid = false;
        } else if (cvv.length() != 3) {
            if (fireError) {
                mCvvField.setError(true);
            }
            isValid = false;
        } else {
            mCvvField.setChecked(true);
        }
        if (card != null) {
            card.cvv = cvv;
        }
        return isValid;
    }

    private boolean checkCardNumber(PaymentCard card, boolean fireError) {
        boolean isValid = true;
        String number = mCardNumberField.getText().toString().replace(" ", "");

        if (TextUtils.isEmpty(number)) {
            if (fireError) {
                mCardNumberField.setError(true);
            }
            isValid = false;
        } else if (!checkIfCardNumberIsMod10(number)) {
            isValid = false;
            mCardNumberField.setError(true);
        } else {
            mCardNumberField.setChecked(true);
        }
        if (card != null) {
            card.number = number;
        }
        return isValid;
    }

    private boolean checkExpiryDate(PaymentCard card, boolean fireError) {
        boolean isValid = true;
        String date = mExpiryField.getText().toString();
        String expMonth = null;
        String expYear = null;

        if (TextUtils.isEmpty(date)) {
            if (fireError) {
                mExpiryField.setError(true);
            }
            isValid = false;
        } else if (date.length() != 5) {
            mExpiryField.setError(true);
            isValid = false;
        } else {
            expMonth = date.substring(0, 2);
            expYear = date.substring(3);
            if (!checkIfExpiryDateIsValid(expMonth, expYear)) {
                isValid = false;
                mExpiryField.setError(true);
            } else {
                mExpiryField.setChecked(true);
            }
        }
        if (card != null) {
            card.expMonth = expMonth;
            card.expYear = expYear;
        }
        return isValid;
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
        if (cardNumber.length() < 16 || !TextUtils.isDigitsOnly(cardNumber)) {
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

            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, false);
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false);
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_ZIP, false);
            scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true);
            startActivityForResult(scanIntent, REQUEST_SCAN_CARD);
        }
    };

    private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (v == mCardNumberField) {
                checkCardNumber(null, false);
            } else if (v == mExpiryField) {
                checkExpiryDate(null, false);
            } else if (v == mCvvField) {
                checkCvv(null, false);
            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            validate(false);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int result, Intent data) {
        switch (requestCode) {
            case REQUEST_SCAN_CARD:
                if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                    CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                    mCardNumberField.setText(scanResult.cardNumber);
                    // mCvvField.setText(scanResult.cvv);
                    // mExpiryField.setText(String.format("%02d/%02d",
                    // scanResult.expiryMonth, scanResult.expiryYear % 100));
                    mExpiryField.requestFocus();
                }
                break;
        }
    };

    private OnClickListener mOnValidateClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (validate(true)) {
                Intent data = new Intent();
                data.putExtra(EXTRA_PAYMENT_CARD, mPaymentCard);
                setResult(RESULT_OK, data);
                finish();
            }
        }
    };

}
