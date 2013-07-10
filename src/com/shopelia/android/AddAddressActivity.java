package com.shopelia.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.AddressesAPI;
import com.shopelia.android.remote.api.ApiHandler.CallbackAdapter;
import com.shopelia.android.remote.api.PlacesAutoCompleteAPI;
import com.shopelia.android.remote.api.PlacesAutoCompleteAPI.OnAddressDetailsListener;
import com.shopelia.android.utils.LocaleUtils;
import com.shopelia.android.widget.AutoCompletionAdapter;
import com.shopelia.android.widget.FormAutocompleteEditText;
import com.shopelia.android.widget.ValidationButton;
import com.shopelia.android.widget.form.EditTextField;
import com.shopelia.android.widget.form.EditTextField.OnValidateListener;
import com.shopelia.android.widget.form.FormField;
import com.shopelia.android.widget.form.FormField.ListenerAdapter;
import com.shopelia.android.widget.form.FormLinearLayout;
import com.shopelia.android.widget.form.NameField;
import com.shopelia.android.widget.form.NumberField;
import com.shopelia.android.widget.form.PhoneField;

public class AddAddressActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "Add Address";

    /*
     * Form informations extras/saves
     */
    public static final String EXTRA_ID = Config.EXTRA_PREFIX + "ID";
    public static final String EXTRA_LASTNAME = Config.EXTRA_PREFIX + "LASTNAME";
    public static final String EXTRA_FIRSTNAME = Config.EXTRA_PREFIX + "FIRSTNAME";
    public static final String EXTRA_ADDRESS = Config.EXTRA_PREFIX + "ADDRESS";
    public static final String EXTRA_ADDRESS_EXTRAS = Config.EXTRA_PREFIX + "ADDRESS_EXTRAS";
    public static final String EXTRA_ZIPCODE = Config.EXTRA_PREFIX + "ZIPCODE";
    public static final String EXTRA_CITY = Config.EXTRA_PREFIX + "CITY";
    public static final String EXTRA_COUNTRY = Config.EXTRA_PREFIX + "COUNTRY";
    public static final String EXTRA_REFERENCE = Config.EXTRA_PREFIX + "REFERENCE";
    public static final String EXTRA_PHONE = Config.EXTRA_PREFIX + "PHONE";

    public static final String EXTRA_ADDRESS_OBJECT = Config.EXTRA_PREFIX + "OBJECT";
    public static final String EXTRA_MODE = Config.EXTRA_PREFIX + "MODE";
    public static final String EXTRA_REQUIRED = Config.EXTRA_PREFIX + "REQUIRED";

    public static final String LOG_TAG = "AddAddressActivity";

    public static final int MODE_CREATE = 0x0;
    public static final int MODE_ADD = 0x1;
    public static final int MODE_EDIT = 0x2;

    private static final SparseArray<String> TRACKER_NAME;

    static {
        TRACKER_NAME = new SparseArray<String>();
        TRACKER_NAME.put(R.id.name, Analytics.Events.UserInteractions.Fields.NAME);
        TRACKER_NAME.put(R.id.address, Analytics.Events.UserInteractions.Fields.ADDRESS_1);
        TRACKER_NAME.put(R.id.extras, Analytics.Events.UserInteractions.Fields.ADDRESS_2);
        TRACKER_NAME.put(R.id.country, Analytics.Events.UserInteractions.Fields.COUNTRY);
        TRACKER_NAME.put(R.id.city, Analytics.Events.UserInteractions.Fields.CITY);
        TRACKER_NAME.put(R.id.zipcode, Analytics.Events.UserInteractions.Fields.ZIP);
    }

    // Views
    private FormAutocompleteEditText mAddressField;
    private FormLinearLayout mFormLayout;

    // Backend
    private LayoutInflater mLayoutInflater;
    private AutocompletionAdapter mAutocompletionAdapter = new AutocompletionAdapter(null);

    private Address mResult = new Address();

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);

        setHostContentView(R.layout.shopelia_add_address_activity);
        mLayoutInflater = LayoutInflater.from(this);

        mFormLayout = (FormLinearLayout) findViewById(R.id.form);

        Bundle extras = getIntent().getExtras() != null ? getIntent().getExtras() : new Bundle();
        String name = (extras.getString(EXTRA_FIRSTNAME) + " " + extras.getString(EXTRA_LASTNAME)).trim();
        name = extras.getString(EXTRA_FIRSTNAME) == null || extras.getString(EXTRA_LASTNAME) == null ? "" : name;
        //@formatter:off
        mFormLayout.findFieldById(R.id.name, NameField.class)
            .setJsonPath(Address.Api.LASTNAME)
            .mandatory()
            .setContentText(name)
            .setListener(mTrackingListener)
            .setOnClickListener(mOnClickListener);
        mFormLayout.findFieldById(R.id.city, NameField.class)
            .setJsonPath(Address.Api.CITY)
            .mandatory()
            .setContentText(extras.getString(EXTRA_CITY))
            .setListener(mTrackingListener)
            .setOnClickListener(mOnClickListener);
        mFormLayout.findFieldById(R.id.extras, EditTextField.class)
            .setJsonPath(Address.Api.EXTRAS)
            .setContentText(extras.getString(EXTRA_ADDRESS_EXTRAS))
            .setListener(mTrackingListener)
            .setOnClickListener(mOnClickListener);
        mFormLayout.findFieldById(R.id.country, EditTextField.class)
            .setJsonPath(Address.Api.COUNTRY)
            .mandatory()
            .setContentText(new Locale("", !TextUtils.isEmpty(extras.getString(EXTRA_COUNTRY)) ? extras.getString(EXTRA_COUNTRY) : Locale.getDefault().getCountry()).getDisplayCountry())
            .setListener(mTrackingListener)
            .setOnClickListener(mOnClickListener);;
        mFormLayout.findFieldById(R.id.country, EditTextField.class)
            .setOnValidateListener(mOnCountryValidateListener)
            .setListener(mTrackingListener)
            .setOnClickListener(mOnClickListener);
        mFormLayout.findFieldById(R.id.country, EditTextField.class)
            .getEditText()
            .setAdapter(new AutoCompletionAdapter<String>(this, R.layout.shopelia_autocompletion_list_item, LocaleUtils.getCountries()));
        mFormLayout.findFieldById(R.id.zipcode, NumberField.class)
            .setMinLength(5)
            .setJsonPath(Address.Api.ZIP)
            .mandatory()
            .setContentText(extras.getString(EXTRA_ZIPCODE))
            .setListener(mTrackingListener)
            .setOnClickListener(mOnClickListener);
        mFormLayout.findFieldById(R.id.phone, PhoneField.class)
            .mandatory()
            .setJsonPath(Address.Api.PHONE)
            .setContentText(extras.getString(EXTRA_PHONE))
            .setListener(mTrackingListener)
            .setOnClickListener(mOnClickListener);
        mFormLayout.findFieldById(R.id.address, EditTextField.class)
            .setJsonPath(Address.Api.ADDRESS1)
            .mandatory()
            .setListener(mTrackingListener)
            .setOnClickListener(mOnClickListener);
        mAddressField = (FormAutocompleteEditText) mFormLayout.findFieldById(R.id.address).getEditText();
        //@formatter:on
        mAddressField.setAdapter(mAutocompletionAdapter);
        mAddressField.setOnItemClickListener(mOnSuggestionClickListener);

        if (getActivityMode() == MODE_CREATE) {
            mFormLayout.removeField(R.id.phone);
        } else {

        }

        mFormLayout.commit();
        mFormLayout.onCreate(saveState);

        ValidationButton validationButton = (ValidationButton) findViewById(R.id.validate);
        validationButton.setOnClickListener(mOnValidateClickListener);
        if (getActivityMode() == MODE_ADD) {
            validationButton.setText(R.string.shopelia_form_address_validate_add);
        } else if (getActivityMode() == MODE_EDIT) {
            validationButton.setText(R.string.shopelia_form_address_validate_edit);
        }
        initUi(saveState == null ? getIntent().getExtras() : saveState);

        if (getActivityMode() == MODE_EDIT) {
            mResult = getIntent().getParcelableExtra(EXTRA_ADDRESS_OBJECT);
        }

    }

    private void initUi(Bundle bundle) {

        if (bundle != null) {
            mAddressField.setText(bundle.getString(EXTRA_ADDRESS));
            mAddressField.setTag(bundle.getString(EXTRA_REFERENCE));
        }

        if (!TextUtils.isEmpty(mAddressField.getText()) && mAddressField.getTag() == null) {
        } else {
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

    protected int getActivityMode() {
        return getIntent().getIntExtra(EXTRA_MODE, MODE_CREATE);
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
                convertView = mLayoutInflater.inflate(R.layout.shopelia_autocompletion_list_item, viewGroup, false);
                ViewHolder holder = new ViewHolder();
                convertView.setTag(holder);
                holder.description = (TextView) convertView.findViewById(android.R.id.text1);
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
            Address address = (Address) mAutocompletionAdapter.getItem(position);
            try {
                int index = address.address.lastIndexOf(',');
                index = address.address.lastIndexOf(',', index - 1);
                mAddressField.setText(address.address.subSequence(0, index));
            } catch (Exception e) {
                // Do nothing
            }
            startWaiting(getString(R.string.shopelia_form_address_loading_autocomplete), true, false);
            AddAddressActivity.this.closeSoftKeyboard();
            mAddressField.clearFocus();
            PlacesAutoCompleteAPI.getAddressDetails(AddAddressActivity.this, address.reference, new OnAddressDetailsListener() {

                @Override
                public void onError() {
                    stopWaiting();
                }

                @Override
                public void onAddressDetails(Address address) {
                    stopWaiting();
                    mFormLayout.findFieldById(R.id.zipcode, EditTextField.class).setContentText(address.zipcode);
                    mFormLayout.findFieldById(R.id.city, EditTextField.class).setContentText(address.city);
                    mFormLayout.findFieldById(R.id.address, EditTextField.class).setContentText(address.address);
                    mFormLayout.findFieldById(R.id.country, EditTextField.class).setContentText(
                            LocaleUtils.getCountryDisplayName(address.country));
                }
            });

        }

    };

    private OnClickListener mOnValidateClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (validate(true)) {
                closeSoftKeyboard();
                switch (getActivityMode()) {
                    case MODE_ADD:
                        addAddress();
                        break;

                    case MODE_EDIT:
                        editAddress();
                        break;

                    default:
                        createAddress();
                        break;
                }
            }
        }

        public void createAddress() {
            Intent data = new Intent();
            Bundle extras = new Bundle();
            extras.putParcelable(EXTRA_ADDRESS_OBJECT, mResult);
            data.putExtras(extras);
            setResult(RESULT_OK, data);
            String method = TextUtils.isEmpty(mResult.reference) ? Analytics.Properties.AddAddressMethod.MANUAL
                    : Analytics.Properties.AddAddressMethod.PLACES_AUTOCOMPLETE;
            // @formatter:on
            finish();
        }

        public void addAddress() {
            startWaiting(getString(R.string.shopelia_form_address_wait_for_adding), true, false);
            new AddressesAPI(AddAddressActivity.this, new CallbackAdapter() {

                public void onAddressAdded(Address address) {
                    stopWaiting();
                    User user = UserManager.get(AddAddressActivity.this).getUser();
                    user.addAddress(address);
                    UserManager.get(AddAddressActivity.this).saveUser();
                    createAddress();
                };

                public void onError(int step, com.turbomanage.httpclient.HttpResponse httpResponse, org.json.JSONObject response,
                        Exception e) {
                    stopWaiting();
                };

            }).addAddress(mResult);
        }

        public void editAddress() {
            startWaiting(getString(R.string.shopelia_form_address_loading_editing), true, false);
            long id = getIntent().getLongExtra(EXTRA_ID, Address.NO_ID);
            mResult.id = id;
            new AddressesAPI(AddAddressActivity.this, new CallbackAdapter() {

                public void onAddressEdited(Address address) {
                    stopWaiting();
                    User user = UserManager.get(AddAddressActivity.this).getUser();
                    long id = getIntent().getLongExtra(EXTRA_ID, Address.NO_ID);
                    for (Address item : user.addresses) {
                        if (item.id == id && id != Address.NO_ID) {
                            item.merge(mResult);
                            break;
                        }
                    }
                    UserManager.get(AddAddressActivity.this).saveUser();
                    createAddress();
                };

                public void onError(int step, com.turbomanage.httpclient.HttpResponse httpResponse, org.json.JSONObject response,
                        Exception e) {
                    stopWaiting();
                };

            }).editAddress(mResult);
        }

    };

    public boolean isRequired() {
        return getIntent().getBooleanExtra(EXTRA_REQUIRED, false);
    }

    private boolean validate(boolean fireError) {
        boolean out = mFormLayout.validate();
        if (out) {
            try {
                mResult = Address.inflate(mFormLayout.toJson());
                mResult.country = LocaleUtils.getCountryISO2Code(mResult.country);

                int first_space = mResult.lastname.indexOf(' ');
                if (first_space != -1) {
                    mResult.firstname = mResult.lastname.substring(0, first_space).trim();
                    mResult.lastname = mResult.lastname.substring(first_space + 1).trim();
                }
                mResult.is_default = true;
                if (TextUtils.isEmpty(mResult.lastname) || TextUtils.isEmpty(mResult.firstname)) {
                    out = false;
                    mFormLayout.findFieldById(R.id.name, EditTextField.class)
                            .setError(getString(R.string.shopelia_form_address_error_name));
                }

            } catch (JSONException e) {
                if (Config.INFO_LOGS_ENABLED) {
                    e.printStackTrace();
                }
            }

        }
        return out;
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            String action = TRACKER_NAME.get(v.getId());
            if (action != null) {
                getTracker().onFocusIn(action);
            }
        }
    };

    private FormField.ListenerAdapter mTrackingListener = new ListenerAdapter() {

        @Override
        public void onValidChanged(FormField field) {
            String event = TRACKER_NAME.get(field.getId());
            if (event != null && field.isValid() && field.getResult() != null) {
                getTracker().onValidate(event);
                field.setListener(null);
            }
        };

    };

    private OnValidateListener mOnCountryValidateListener = new OnValidateListener() {

        @Override
        public boolean onValidate(EditTextField editTextField, boolean shouldFireError) {
            boolean isValid = LocaleUtils.getCountryISO2Code((String) editTextField.getResult()) != null;
            if (shouldFireError && !isValid) {
                editTextField.setError(getString(R.string.shopelia_form_address_country_not_found));
            }
            return isValid;
        }
    };

}
