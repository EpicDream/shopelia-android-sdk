package com.shopelia.android.adapter.form;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.shopelia.android.CreateAddressActivity;
import com.shopelia.android.model.Address;

public class AddressField extends ButtonField {

    public static final int TYPE = 2;
    public static int REQUEST_ADDRESS = 0x16;

    private Address mAddress = null;

    public AddressField(Context context, int resId) {
        super(context, resId);
    }

    @Override
    public Object getResult() {
        try {
            return mAddress != null ? mAddress.toJson() : null;
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(getJsonPath(), mAddress);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mAddress = savedInstanceState.getParcelable(getJsonPath());
            if (mAddress != null) {
                setContentText(mAddress.toString());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADDRESS && resultCode == Activity.RESULT_OK) {
            mAddress = data.getParcelableExtra(CreateAddressActivity.EXTRA_ADDRESS_OBJECT);
            setContentText(mAddress.toString());
            setValid(true);
            getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }

    @Override
    protected void onClick(Button view) {
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            Intent intent = new Intent(activity, CreateAddressActivity.class);
            if (mAddress != null) {
                intent.putExtra(CreateAddressActivity.EXTRA_ADDRESS, mAddress.address);
                intent.putExtra(CreateAddressActivity.EXTRA_ZIPCODE, mAddress.zipcode);
                intent.putExtra(CreateAddressActivity.EXTRA_ADDRESS_EXTRAS, mAddress.extras);
                intent.putExtra(CreateAddressActivity.EXTRA_CITY, mAddress.city);
                intent.putExtra(CreateAddressActivity.EXTRA_COUNTRY, mAddress.country);
                intent.putExtra(CreateAddressActivity.EXTRA_FIRSTNAME, mAddress.firstname);
                intent.putExtra(CreateAddressActivity.EXTRA_NAME, mAddress.name);
            }
            activity.startActivityForResult(intent, REQUEST_ADDRESS);
        }
    }

}
