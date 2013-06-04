package com.shopelia.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.analytics.AnalyticsBuilder;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.Address;
import com.shopelia.android.remote.api.PlacesAutoCompleteAPI;
import com.shopelia.android.utils.LocaleUtils;
import com.shopelia.android.widget.Errorable;
import com.shopelia.android.widget.FormEditText;

public class AddAddressActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "Add Address";

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
    public static final String EXTRA_REFERENCE = Config.EXTRA_PREFIX + "REFERENCE";

    public static final String EXTRA_ADDRESS_OBJECT = Config.EXTRA_PREFIX + "OBJECT";

    public static final String LOG_TAG = "AddAddressActivity";

    private static final SparseArray<String> CLICK_ON;

    static {
        CLICK_ON = new SparseArray<String>();
        CLICK_ON.put(R.id.first_name, Analytics.Properties.ClickOn.SigningUp.ADDRESS_NAME);
        CLICK_ON.put(R.id.name, Analytics.Properties.ClickOn.SigningUp.ADDRESS_LAST_NAME);
        CLICK_ON.put(R.id.address, Analytics.Properties.ClickOn.SigningUp.ADDRESS_LINE_1);
        CLICK_ON.put(R.id.extras, Analytics.Properties.ClickOn.SigningUp.ADDRESS_LINE_2);
        CLICK_ON.put(R.id.country, Analytics.Properties.ClickOn.SigningUp.ADDRESS_COUNTRY);
        CLICK_ON.put(R.id.city, Analytics.Properties.ClickOn.SigningUp.ADDRESS_CITY);
        CLICK_ON.put(R.id.zipcode, Analytics.Properties.ClickOn.SigningUp.ADDRESS_ZIP);
    }

    // Views
    private AutoCompleteTextView mAddressField;
    private FormEditText mNameField;
    private FormEditText mFirstNameField;
    private FormEditText mAddressExtrasField;
    private FormEditText mPostalCodeField;
    private FormEditText mCityField;
    private FormEditText mCountryField;

    private ImageView mHeaderIcon;
    private TextView mHeaderTitle;

    private String mReferencedText;

    // Backend
    private LayoutInflater mLayoutInflater;
    private AutocompletionAdapter mAutocompletionAdapter = new AutocompletionAdapter(null);

    private Address mResult = new Address();

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);

        setHostContentView(R.layout.shopelia_create_address_activity);
        mLayoutInflater = LayoutInflater.from(this);

        mNameField = (FormEditText) findViewById(R.id.name);
        mFirstNameField = (FormEditText) findViewById(R.id.first_name);
        mCityField = (FormEditText) findViewById(R.id.city);
        mAddressField = (AutoCompleteTextView) findViewById(R.id.address);
        mAddressExtrasField = (FormEditText) findViewById(R.id.extras);
        mCountryField = (FormEditText) findViewById(R.id.country);
        mPostalCodeField = (FormEditText) findViewById(R.id.zipcode);

        mAddressField.setAdapter(mAutocompletionAdapter);
        mAddressField.setOnItemClickListener(mOnSuggestionClickListener);

        // Add focus change listener for automatic validation
        mNameField.setOnFocusChangeListener(mOnFocusChangeListener);
        mFirstNameField.setOnFocusChangeListener(mOnFocusChangeListener);
        mCityField.setOnFocusChangeListener(mOnFocusChangeListener);
        mAddressField.setOnFocusChangeListener(mOnFocusChangeListener);
        mAddressExtrasField.setOnFocusChangeListener(mOnFocusChangeListener);
        mCountryField.setOnFocusChangeListener(mOnFocusChangeListener);
        mPostalCodeField.setOnFocusChangeListener(mOnFocusChangeListener);

        mAddressField.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (mReferencedText != null && !s.toString().equals(mReferencedText)) {
                    mAddressField.setTag(null);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

        });

        mAddressExtrasField.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && mCityField.getVisibility() != View.VISIBLE) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mAddressExtrasField.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        View headerFrame = findViewById(R.id.header_frame);
        mHeaderIcon = (ImageView) headerFrame.findViewById(R.id.icon);
        mHeaderTitle = (TextView) headerFrame.findViewById(R.id.title);

        findViewById(R.id.validate).setOnClickListener(mOnValidateClickListener);

        initUi(saveState == null ? getIntent().getExtras() : saveState);
    }

    private void initUi(Bundle bundle) {

        if (bundle != null) {
            mNameField.setText(bundle.getString(EXTRA_NAME));
            mFirstNameField.setText(bundle.getString(EXTRA_FIRSTNAME));
            mAddressField.setText(bundle.getString(EXTRA_ADDRESS));
            mAddressExtrasField.setText(bundle.getString(EXTRA_ADDRESS_EXTRAS));
            mPostalCodeField.setText(bundle.getString(EXTRA_ZIPCODE));
            mCityField.setText(bundle.getString(EXTRA_CITY));
            if (bundle.containsKey(EXTRA_COUNTRY)) {
                mCountryField.setText(new Locale("", bundle.getString(EXTRA_COUNTRY)).getDisplayCountry());
            }
            mAddressField.setTag(bundle.getString(EXTRA_REFERENCE));
            mReferencedText = bundle.getString(EXTRA_REFERENCE);
        }

        if (!TextUtils.isEmpty(mAddressField.getText()) && mAddressField.getTag() == null) {
            mPostalCodeField.setVisibility(View.VISIBLE);
            mCityField.setVisibility(View.VISIBLE);
            mCountryField.setVisibility(View.VISIBLE);
        } else {
            mPostalCodeField.setVisibility(View.GONE);
            mCityField.setVisibility(View.GONE);
            mCountryField.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(mCountryField.getText())) {
            mCountryField.setText(Locale.getDefault().getDisplayCountry());
        }

        mHeaderIcon.setImageResource(R.drawable.shopelia_pin);
        mHeaderTitle.setText(R.string.shopelia_form_main_shipping_address);

        validate(false);
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
        outState.putString(EXTRA_REFERENCE, (String) mAddressField.getTag());
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
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

        private final Filter FILTER = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraints) {
                List<Address> addresses = PlacesAutoCompleteAPI.autocomplete(AddAddressActivity.this,
                        constraints != null ? constraints.toString() : "", 0);
                FilterResults results = new FilterResults();
                results.values = addresses;
                results.count = addresses != null ? addresses.size() : 0;
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraints, FilterResults results) {
                mAddressList = (List<Address>) results.values;
                notifyDataSetChanged();
            }

        };

    }

    private OnItemClickListener mOnSuggestionClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long itemId) {
            mAddressExtrasField.requestFocus();
            Address address = (Address) mAutocompletionAdapter.getItem(position);
            mPostalCodeField.setVisibility(View.GONE);
            mCityField.setVisibility(View.GONE);
            mCountryField.setVisibility(View.GONE);
            mAddressField.setTag(address.reference);
            mReferencedText = address.address;
            validate(false);
        }

    };

    private OnClickListener mOnValidateClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (validate(true)) {
                closeSoftKeyboard();
                Intent data = new Intent();
                Bundle extras = new Bundle();
                extras.putParcelable(EXTRA_ADDRESS_OBJECT, mResult);
                data.putExtras(extras);
                setResult(RESULT_OK, data);
                String method = TextUtils.isEmpty(mResult.reference) ? Analytics.Properties.AddAddressMethod.MANUAL
                        : Analytics.Properties.AddAddressMethod.PLACES_AUTOCOMPLETE;
                track(Analytics.Events.ADD_ADDRESS_METHOD, AnalyticsBuilder.prepareMethodPackage(AddAddressActivity.this, method));
                finish();
            }
        }
    };

    private boolean validate(boolean fireError) {
        mResult = new Address();
        mResult.name = mNameField.getText().toString();
        mResult.firstname = mFirstNameField.getText().toString();
        mResult.address = mAddressField.getText().toString();
        mResult.extras = mAddressExtrasField.getText().toString();
        mResult.zipcode = mPostalCodeField.getText().toString();
        mResult.city = mCityField.getText().toString();
        mResult.country = LocaleUtils.getCountryISO2Code(mCountryField.getText().toString());
        if (mAddressField.getTag() != null) {
            mResult.reference = (String) mAddressField.getTag();
        }
        boolean out = mResult.reference != null ? validateFields(fireError, mAddressField, mNameField, mFirstNameField) : validateFields(
                fireError, mAddressField, mNameField, mFirstNameField, mCityField, mCountryField, mPostalCodeField);
        if (out) {
            mHeaderTitle.setTextColor(getResources().getColor(R.color.shopelia_headerTitleSectionOkColor));
            mHeaderIcon.setImageResource(R.drawable.shopelia_check_ok);
        } else {
            mHeaderTitle.setTextColor(getResources().getColor(R.color.shopelia_headerTitleSectionRegularColor));
            mHeaderIcon.setImageResource(R.drawable.shopelia_pin);
        }
        if (fireError && TextUtils.isEmpty(mResult.country) && mResult.reference == null) {
            mCountryField.setError(true);
        }
        return out;
    }

    private static boolean validateFields(boolean fireError, EditText... fields) {
        boolean out = true;
        for (int index = 0; index < fields.length; index++) {
            if (TextUtils.isEmpty(fields[index].getText().toString())) {
                out = false;
                if (fields[index] instanceof Checkable) {
                    ((Checkable) fields[index]).setChecked(false);
                }

                if (fireError && fields[index] instanceof Errorable) {
                    ((Errorable) fields[index]).setError(true);
                }
            } else {
                if (fields[index] instanceof Checkable) {
                    ((Checkable) fields[index]).setChecked(true);
                }
                if (fireError && fields[index] instanceof Errorable) {
                    ((Errorable) fields[index]).setError(false);
                }
            }
        }
        return out;
    }

    private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            validate(false);
            if (hasFocus) {
                String action = CLICK_ON.get(v.getId());
                if (action != null) {
                    track(Analytics.Events.Steps.SignUp.SIGN_UP_ACTION,
                            AnalyticsBuilder.prepareClickOnPackage(AddAddressActivity.this, action));
                }
            }
            if (v == mAddressField && !TextUtils.isEmpty(mAddressField.getText()) && v.getTag() == null) {
                mPostalCodeField.setVisibility(View.VISIBLE);
                mCityField.setVisibility(View.VISIBLE);
                mCountryField.setVisibility(View.VISIBLE);
            }

        }
    };

}