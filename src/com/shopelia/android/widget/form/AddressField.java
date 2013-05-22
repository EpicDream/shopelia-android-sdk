package com.shopelia.android.widget.form;

import java.util.Locale;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.Button;

import com.shopelia.android.CreateAddressActivity;
import com.shopelia.android.R;
import com.shopelia.android.model.Address;

public class AddressField extends ButtonField {

    public static final int TYPE = 2;
    public static int REQUEST_ADDRESS = 0x16;

    private Address mAddress = null;

    public AddressField(Context context) {
        this(context, null);
    }

    public AddressField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddressField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
        setValid(mAddress != null);
        setChecked(isValid());
        setError(!isValid());
        return isValid();
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
                setAddress(mAddress);
            }
        }
    }

    public void setAddress(Address address) {
        if (address == null || !address.isValid()) {
            return;
        }
        mAddress = address;
        if (mAddress.reference == null) {
            setContentText(getContext().getString(R.string.shopelia_form_address_display_format, mAddress.address, mAddress.city,
                    new Locale("", mAddress.country).getDisplayCountry()));
        } else {
            setContentText(mAddress.toString());
        }
        setValid(true);
        setChecked(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADDRESS && resultCode == Activity.RESULT_OK) {
            setAddress((Address) data.getParcelableExtra(CreateAddressActivity.EXTRA_ADDRESS_OBJECT));
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
                intent.putExtra(CreateAddressActivity.EXTRA_REFERENCE, mAddress.reference);
            }
            activity.startActivityForResult(intent, REQUEST_ADDRESS);
        }
    }

}
