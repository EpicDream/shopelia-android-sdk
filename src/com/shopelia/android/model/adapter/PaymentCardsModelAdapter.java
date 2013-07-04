package com.shopelia.android.model.adapter;

import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shopelia.android.R;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.utils.FormatUtils;

public class PaymentCardsModelAdapter extends BaseModelAdapter<PaymentCard> {

    public PaymentCardsModelAdapter(Context context) {
        super(context);
    }

    @Override
    public void setContent(JSONObject root) {

    }

    @Override
    public void bindView(View v, PaymentCard data) {
        ViewHolder holder = (ViewHolder) v.getTag();
        holder.isDefault.setVisibility(data.getId() == getDefaultId() ? View.VISIBLE : View.INVISIBLE);
        if (data.number.substring(0, 1).equals("4")) {
            holder.brandLogo.setImageResource(R.drawable.shopelia_ic_visa);
            holder.brandLogo.setVisibility(View.VISIBLE);
        } else if (data.number.substring(0, 1).equals("5")) {
            holder.brandLogo.setImageResource(R.drawable.shopelia_ic_mastercard);
            holder.brandLogo.setVisibility(View.VISIBLE);
        } else {
            holder.brandLogo.setImageResource(R.drawable.shopelia_ic_mastercard);
            holder.brandLogo.setVisibility(View.INVISIBLE);
        }
        holder.cardNumber.setText(FormatUtils.formatCardNumber(data.number, '*', 4, 8));
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        View v = inflater.inflate(R.layout.shopelia_list_item_payment_card, container, false);
        ViewHolder holder = new ViewHolder();
        holder.brandLogo = (ImageView) v.findViewById(R.id.card_brand_logo);
        holder.isDefault = (ImageView) v.findViewById(R.id.is_default);
        holder.cardNumber = (TextView) v.findViewById(R.id.payment_card_number);
        v.setTag(holder);
        return v;
    }

    private static class ViewHolder {
        public ImageView brandLogo;
        public ImageView isDefault;
        public TextView cardNumber;
    }

}
