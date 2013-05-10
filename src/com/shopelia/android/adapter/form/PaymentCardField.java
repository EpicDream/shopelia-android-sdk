package com.shopelia.android.adapter.form;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.shopelia.android.AddPaymentCardActivity;
import com.shopelia.android.model.PaymentCard;

public class PaymentCardField extends ButtonField {

    private static final int REQUEST_CARD = 16;

    private PaymentCard mPaymentCard;

    public PaymentCardField(Context context, int resId) {
        super(context, resId);
    }

    @Override
    protected void onClick(Button view) {
        Activity activity = (Activity) getContext();
        Intent intent = new Intent(activity, AddPaymentCardActivity.class);
        intent.putExtra(AddPaymentCardActivity.EXTRA_PAYMENT_CARD, mPaymentCard);
        activity.startActivityForResult(intent, REQUEST_CARD);
    }

    @Override
    public Object getResult() {
        try {
            if (mPaymentCard != null) {
                return mPaymentCard.toJson();
            }
        } catch (JSONException e) {
            // Do nothing just wait for return null
        }
        return null;
    }

    @Override
    public boolean validate() {
        setValid(mPaymentCard != null);
        setChecked(isValid());
        setError(!isValid());
        return isValid();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CARD && resultCode == Activity.RESULT_OK) {
            mPaymentCard = data.getParcelableExtra(AddPaymentCardActivity.EXTRA_PAYMENT_CARD);
            setPaymentCard(mPaymentCard);
        }
    }

    private void setPaymentCard(PaymentCard card) {
        mPaymentCard = card;
        setValid(mPaymentCard != null);
        setChecked(mPaymentCard != null);
        setDisplayableCardNumber();
        getAdapter().updateSections();
        getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(getJsonPath(), mPaymentCard);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPaymentCard = savedInstanceState.getParcelable(getJsonPath());
            setPaymentCard(mPaymentCard);
        }
    }

    @Override
    public void bindView(View view) {
        setDisplayableCardNumber();
        super.bindView(view);
    }

    private void setDisplayableCardNumber() {
        if (mPaymentCard != null) {
            StringBuilder number = new StringBuilder(mPaymentCard.number);
            int relativeIndex = 0;
            for (int index = 0; index < number.length(); index++) {
                if (index < number.length() - 4) {
                    number.replace(index, index + 1, "*");
                }
                if (index > 0 && relativeIndex % 4 == 0) {
                    number.insert(index, " ");
                    index++;
                    relativeIndex = 0;
                }
                relativeIndex++;
            }
            setContentText(number.toString());
        }
    }
}
