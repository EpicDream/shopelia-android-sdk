package com.shopelia.android.model.adapter;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shopelia.android.R;
import com.shopelia.android.model.Address;
import com.shopelia.android.widget.FontableTextView;

public class AddressModelAdapter extends BaseModelAdapter<Address> {

    public AddressModelAdapter(Context context) {
        super(context);
    }

    @Override
    public void setContent(JSONObject root) {

    }

    @Override
    public void bindView(View v, Address data) {
        ViewHolder holder = (ViewHolder) v.getTag();
        holder.address.setText(data.address);
        holder.city_and_country.setText(data.zipcode + ", " + data.city + ", " + data.getDisplayCountry());
        holder.extras.setText(data.extras);
        holder.extras.setVisibility(TextUtils.isEmpty(holder.extras.getText()) ? View.GONE : View.VISIBLE);
        holder.phone.setText(data.phone);
        holder.username.setText(data.firstname + " " + data.name);
        holder.username.setVisibility(TextUtils.isEmpty(data.firstname) ? View.GONE : View.VISIBLE);
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        View v = inflater.inflate(R.layout.shopelia_list_item_address, container, false);
        ViewHolder holder = new ViewHolder();
        holder.username = (FontableTextView) v.findViewById(R.id.address_user_name);
        holder.address = (FontableTextView) v.findViewById(R.id.address_address);
        holder.city_and_country = (FontableTextView) v.findViewById(R.id.address_city_and_country);
        holder.extras = (FontableTextView) v.findViewById(R.id.address_extras);
        holder.phone = (FontableTextView) v.findViewById(R.id.user_phone_number);
        v.setTag(holder);
        return v;
    }

    private static class ViewHolder {
        public FontableTextView username;
        public FontableTextView address;
        public FontableTextView extras;
        public FontableTextView phone;
        public FontableTextView city_and_country;
    }

}
