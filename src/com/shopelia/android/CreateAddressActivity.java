package com.shopelia.android;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.shopelia.android.api.PlacesAutoCompleteClient;
import com.shopelia.android.app.HostActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.Address;

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

    private static final long REQUEST_DELAY = 400;

    // Views
    private AutoCompleteTextView mAddressField;
    private EditText mNameField;
    private EditText mFirstNameField;
    private EditText mAddressExtrasField;
    private EditText mPostalCodeField;
    private EditText mCityField;
    private EditText mCountryField;

    // Backend
    private long mRequestId;
    private LayoutInflater mLayoutInflater;
    private AutocompletionAdapter mAutocompletionAdapter = new AutocompletionAdapter(null);

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);

        setHostContentView(R.layout.shopelia_create_address_activity);

        mLayoutInflater = LayoutInflater.from(this);

        mNameField = (EditText) findViewById(R.id.name);
        mFirstNameField = (EditText) findViewById(R.id.first_name);
        mCityField = (EditText) findViewById(R.id.city);
        mAddressField = (AutoCompleteTextView) findViewById(R.id.address);
        mAddressExtrasField = (EditText) findViewById(R.id.extras);
        mCountryField = (EditText) findViewById(R.id.country);
        mPostalCodeField = (EditText) findViewById(R.id.zipcode);

        mAddressField.addTextChangedListener(mTextWatcher);

        mAddressField.setAdapter(mAutocompletionAdapter);

        initUi(saveState == null ? getIntent().getExtras() : saveState);

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

    private class AutocompletionAdapter extends BaseAdapter implements Filterable {

        private List<Address> mAddressList = new ArrayList<Address>();

        public AutocompletionAdapter(List<Address> addresses) {
            mAddressList = addresses != null ? addresses : mAddressList;
        }

        @Override
        public int getCount() {
            return mAddressList != null ? mAddressList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mAddressList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.shopelia_form_address_place_item, viewGroup, false);
                ViewHolder holder = new ViewHolder();
                convertView.setTag(holder);
                holder.description = (TextView) convertView.findViewById(R.id.address);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            Address address = (Address) getItem(position);
            holder.description.setText(address.toString());
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return FILTER;
        }

        private class ViewHolder {
            TextView description;
        }

        private Filter FILTER = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraints) {
                Log.d(null, "FILTER");
                List<Address> addresses = PlacesAutoCompleteClient.autocomplete(CreateAddressActivity.this,
                        constraints != null ? constraints.toString() : "", 0);
                FilterResults results = new FilterResults();
                results.values = addresses;
                results.count = addresses != null ? addresses.size() : 0;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraints, FilterResults results) {
                mAddressList = (List<Address>) results.values;
                notifyDataSetChanged();
            }

        };

    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

}