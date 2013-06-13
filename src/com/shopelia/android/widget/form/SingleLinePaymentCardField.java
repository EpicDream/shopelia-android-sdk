package com.shopelia.android.widget.form;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.shopelia.android.R;
import com.shopelia.android.algorithm.Luhn;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.pretty.CardNumberFormattingTextWatcher;
import com.shopelia.android.pretty.DateFormattingTextWatcher;
import com.shopelia.android.text.method.ObfuscationTransformationMethod;
import com.shopelia.android.widget.SegmentedEditText;
import com.shopelia.android.widget.SegmentedEditText.Segment;

public class SingleLinePaymentCardField extends FormField {

    public static final String SAVE_TAG = "EditTextFieldSave_";

    private static final int REQUEST_CARD = 16;

    private static final int MAX_CARD_VALIDITY_YEAR = 15;
    private static final int MAX_MONTH_VALUE = 12;

    private String mJsonPath;

    private Segment mCvvField;
    private Segment mExpiryField;
    private Segment mCardNumberField;
    private TextView mErrorMessage;

    private SegmentedEditText mSegmentedEditText;

    private PaymentCard mPaymentCard;

    public SingleLinePaymentCardField(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public long getItemId() {
        return 0;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, ViewGroup viewGroup) {
        View v = inflater.inflate(R.layout.shopelia_form_field_single_line_payment, viewGroup, false);

        v.findViewById(R.id.scan).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Activity activity = (Activity) getContext();
                Intent scanIntent = new Intent(activity, CardIOActivity.class);

                scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN, Config.CARDIO_TOKEN);

                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, false);
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false);
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_ZIP, false);
                scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true);
                activity.startActivityForResult(scanIntent, REQUEST_CARD);
            }
        });
        mErrorMessage = (TextView) v.findViewById(R.id.error);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            v.findViewById(R.id.scan).setVisibility(View.GONE);
        }

        /*
         * SegmentedEditText initilization
         */

        SegmentedEditText editText = (SegmentedEditText) v.findViewById(R.id.edit_text);
        mSegmentedEditText = editText;

        mCardNumberField = editText.createSegment(R.id.shopelia_card_number);
        mCardNumberField.getView().setHint(R.string.shopelia_form_main_card_number);
        mCardNumberField.setFillParent(true);
        mCardNumberField.getView().setTransformationMethod(new ObfuscationTransformationMethod());
        mCardNumberField.setOnValidateListener(new SegmentedEditText.OnValidateListener() {

            @Override
            public boolean onValidate(Segment segment, CharSequence text) {
                if (checkCardNumber(null, false)) {
                    return true;
                }
                return false;
            }
        });
        mCardNumberField.getView().addTextChangedListener(new CardNumberFormattingTextWatcher());
        mCardNumberField.getView().setFilters(new InputFilter[] {
            new InputFilter.LengthFilter(19)
        });
        mCardNumberField.setObfuscationMethod(new ObfuscationTransformationMethod(15, '*', ' '));
        editText.addSegment(mCardNumberField);

        mExpiryField = editText.createSegment(R.id.shopelia_card_expiry_date);
        mExpiryField.getView().setHint(R.string.shopelia_form_payment_card_expiry_date);
        mExpiryField.getView().addTextChangedListener(new DateFormattingTextWatcher());
        mExpiryField.getView().setFilters(new InputFilter[] {
            new InputFilter.LengthFilter(5)
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mExpiryField.getView().setGravity(Gravity.CENTER);
        }
        mExpiryField.setOnValidateListener(new SegmentedEditText.OnValidateListener() {

            @Override
            public boolean onValidate(Segment segment, CharSequence text) {
                if (checkExpiryDate(null, false)) {
                    return true;
                }
                return false;
            }
        });
        editText.addSegment(mExpiryField);

        mCvvField = editText.createSegment(R.id.shopelia_card_cvv);
        mCvvField.getView().setHint(R.string.shopelia_form_payment_card_cvv);
        mCvvField.getView().setFilters(new InputFilter[] {
            new InputFilter.LengthFilter(3)
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mCvvField.getView().setGravity(Gravity.CENTER);
        }
        mCvvField.getView().setTransformationMethod(PasswordTransformationMethod.getInstance());
        mCvvField.setOnValidateListener(new SegmentedEditText.OnValidateListener() {

            @Override
            public boolean onValidate(Segment segment, CharSequence text) {
                if (checkCvv(null, false)) {
                    InputMethodManager imm = (InputMethodManager) segment.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(segment.getView().getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        editText.addSegment(mCvvField);

        editText.commit();
        editText.setOnErrorListener(mOnErrorListener);
        /*
         * End Of initialization
         */
        return v;
    }

    @Override
    public void bindView(View view) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_TAG + getJsonPath() + PaymentCard.Api.NUMBER, mCardNumberField.getText().toString());
        outState.putString(SAVE_TAG + getJsonPath() + PaymentCard.Api.CVV, mCvvField.getText().toString());
        outState.putString(SAVE_TAG + getJsonPath() + PaymentCard.Api.EXPIRY_DATE, mExpiryField.getText().toString());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCardNumberField.getView().setText(savedInstanceState.getString(SAVE_TAG + getJsonPath() + PaymentCard.Api.NUMBER));
            mCvvField.getView().setText(savedInstanceState.getString(SAVE_TAG + getJsonPath() + PaymentCard.Api.CVV));
            mExpiryField.getView().setText(savedInstanceState.getString(SAVE_TAG + getJsonPath() + PaymentCard.Api.EXPIRY_DATE));
            mCardNumberField.setChecked(mCardNumberField.isValid());
            mCvvField.setChecked(mCvvField.isValid());
            mExpiryField.setChecked(mExpiryField.isValid());
            mSegmentedEditText.invalidateSegments(false);
            mSegmentedEditText.updateState();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int result, Intent data) {
        switch (requestCode) {
            case REQUEST_CARD:
                if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                    CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                    mCardNumberField.getView().setText(CardNumberFormattingTextWatcher.makeNumberPretty(scanResult.cardNumber));
                    // mCardScanned = true;
                    mCardNumberField.nextSegment(true);
                }
                break;
        }
    };

    @Override
    public Object getResult() {
        if (mPaymentCard == null) {
            return new JSONObject();
        }
        try {
            return mPaymentCard.toJson();
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    @Override
    public String getResultAsString() {
        return getResult().toString();
    }

    @Override
    public String getJsonPath() {
        return mJsonPath;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }

    public SingleLinePaymentCardField setJsonPath(String jsonPath) {
        mJsonPath = jsonPath;
        return this;
    }

    @Override
    public boolean validate() {
        boolean isValid = true;
        PaymentCard card = new PaymentCard();

        isValid = checkCardNumber(card, true) && isValid;
        isValid = checkCvv(card, true) && isValid;
        isValid = checkExpiryDate(card, true) && isValid;

        if (isValid) {
            mPaymentCard = card;
        } else {
            mPaymentCard = null;
        }
        mSegmentedEditText.setError(!isValid);
        mSegmentedEditText.setChecked(isValid);
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

        if (TextUtils.isEmpty(number) || number.length() < 16) {
            if (fireError) {
                mCardNumberField.setError(true);
            }
            isValid = false;
            mErrorMessage.setVisibility(View.GONE);
        } else if (!checkIfCardNumberIsMod10(number)) {
            isValid = false;
            mCardNumberField.setError(true);
            mCardNumberField.playWakeUp();
            mErrorMessage.setVisibility(View.GONE);
        } else if (!checkIsCompatibleWithShopelia(number)) {
            isValid = false;
            mCardNumberField.setError(true);
            mCardNumberField.playWakeUp();
            mErrorMessage.setText(R.string.shopelia_form_payment_card_not_compatible);
            mErrorMessage.setVisibility(View.VISIBLE);
        } else {
            mCardNumberField.setChecked(true);
        }
        if (card != null) {
            card.number = number;
            // mErrorMessage.setVisibility(View.GONE);
        }

        return isValid;
    }

    private boolean checkIsCompatibleWithShopelia(String number) {
        return number.charAt(0) == '4' || number.charAt(0) == '5';
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
            // mExpiryField.setError(true);
            isValid = false;
        } else {
            expMonth = date.substring(0, 2);
            expYear = date.substring(3);
            if (!checkIfExpiryDateIsValid(expMonth, expYear)) {
                isValid = false;
                mExpiryField.setError(true);
                mExpiryField.playWakeUp();
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
            int M = Integer.valueOf(month) - 1;
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
                isValid = false;
            }

        } catch (NumberFormatException e) {
            isValid = false;
        }
        return isValid;
    }

    private boolean checkIfCardNumberIsMod10(String cardNumber) {
        cardNumber = cardNumber.replace(" ", "");
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

    private SegmentedEditText.OnErrorListener mOnErrorListener = new SegmentedEditText.OnErrorListener() {

        @Override
        public void onError(Segment s, CharSequence message) {

        }
    };

}
