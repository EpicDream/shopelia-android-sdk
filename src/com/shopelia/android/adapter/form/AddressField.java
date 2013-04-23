package com.shopelia.android.adapter.form;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.CreateAddressActivity;
import com.shopelia.android.R;
import com.shopelia.android.adapter.FormAdapter.Field;

public class AddressField extends Field {

    public static final int TYPE = 2;
    public static int REQUEST_ADDRESS = 0x16;

    private String mJsonPath = "Address";

    public AddressField() {
        super(TYPE);
    }

    @Override
    public long getItemId() {
        return 0;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, ViewGroup viewGroup) {
        View root = inflater.inflate(R.layout.shopelia_form_field_address_field, viewGroup, false);
        ViewHolder holder = new ViewHolder();
        holder.address = (TextView) root.findViewById(R.id.address);
        root.setTag(holder);
        return root;
    }

    @Override
    public void bindView(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.address.setOnClickListener(mOnClickListener);
    }

    @Override
    public Object getResult() {
        return "un petit test de résultat";
    }

    @Override
    public String getJsonPath() {
        return mJsonPath;
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

    @Override
    public boolean isSectionHeader() {
        return false;
    }

    private class ViewHolder {
        TextView address;
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (getContext() instanceof Activity) {
                Activity activity = (Activity) getContext();
                Intent intent = new Intent(activity, CreateAddressActivity.class);
                activity.startActivityForResult(intent, REQUEST_ADDRESS);
            }
        }
    };

}
