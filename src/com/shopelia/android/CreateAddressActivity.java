package com.shopelia.android;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.shopelia.android.app.HostActivity;
import com.shopelia.android.config.Config;

public class CreateAddressActivity extends HostActivity {

    /*
     * Form informations extras/saves
     */
    public static final String EXTRA_NAME = Config.EXTRA_PREFIX + "NAME";
    public static final String EXTRA_FIRSTNAME = Config.EXTRA_PREFIX + "FIRSTNAME";
    public static final String EXTRA_ADDRESS = Config.EXTRA_PREFIX + "ADDRESS";
    public static final String EXTRA_ADDRESS_EXTRAS = Config.EXTRA_PREFIX + "ADDRESS_EXTRAS";
    public static final String EXTRA_ZIPCODE = Config.EXTRA_PREFIX + "ZIPCODE";
    public static final String EXTRA_CITY = Config.EXTRA_PREFIX + "CITY";
    public static final String EXTRA_COUNTRY = Config.EXTRA_PREFIX + "COUNTRY";

    public static final String LOG_TAG = "CreateAddressActivity";

    // Views
    private EditText mAddressField;
    private EditText mNameField;
    private EditText mFirstNameField;
    private EditText mAddressExtrasField;
    private EditText mPostalCodeField;
    private EditText mCityField;
    private EditText mCountryField;

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_create_address_activity);

        mNameField = (EditText) findViewById(R.id.name);
        mFirstNameField = (EditText) findViewById(R.id.first_name);
        mCityField = (EditText) findViewById(R.id.city);
        mAddressField = (EditText) findViewById(R.id.address);
        mAddressExtrasField = (EditText) findViewById(R.id.extras);
        mCountryField = (EditText) findViewById(R.id.country);
        mPostalCodeField = (EditText) findViewById(R.id.zipcode);

        initUi(saveState == null ? getIntent().getExtras() : saveState);

        mAddressField.setOnClickListener(mOnAddressClickListener);

    }

    private void initUi(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mNameField.setText(bundle.getString(EXTRA_NAME));
        mFirstNameField.setText(bundle.getString(EXTRA_FIRSTNAME));
        mAddressField.setText(bundle.getString(EXTRA_ADDRESS));
        mAddressExtrasField.setText(bundle.getString(EXTRA_ADDRESS_EXTRAS));
        mPostalCodeField.setText(bundle.getString(EXTRA_ZIPCODE));
        mCityField.setText(bundle.getString(EXTRA_CITY));
        mCountryField.setText(bundle.getString(EXTRA_COUNTRY));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_NAME, mNameField.getText().toString());
        outState.putString(EXTRA_FIRSTNAME, mFirstNameField.getText().toString());
        outState.putString(EXTRA_ADDRESS, mAddressField.getText().toString());
        outState.putString(EXTRA_ADDRESS_EXTRAS, mAddressExtrasField.getText().toString());
        outState.putString(EXTRA_ZIPCODE, mPostalCodeField.getText().toString());
        outState.putString(EXTRA_CITY, mCityField.getText().toString());
        outState.putString(EXTRA_COUNTRY, mCountryField.getText().toString());
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

    private OnClickListener mOnAddressClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

        }
    };

}
