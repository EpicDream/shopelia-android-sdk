package com.shopelia.android.model.adapter;

import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shopelia.android.R;
import com.shopelia.android.model.PaymentCard;

public class PaymentCardsModelAdapter extends BaseModelAdapter<PaymentCard> {

    public PaymentCardsModelAdapter(Context context) {
        super(context);
    }

    @Override
    public void setContent(JSONObject root) {

    }

    @Override
    public void bindView(View v, PaymentCard data) {

    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.shopelia_add_payment_card_fragment, container, false);
    }

}
