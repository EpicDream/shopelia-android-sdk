package com.shopelia.android;

import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.shopelia.android.SignUpFragment.OnSignUpListener;
import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.analytics.AnalyticsBuilder;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.shopelia.android.widget.ValidationButton;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.TextButtonItem;
import com.shopelia.android.widget.form.AddressField;
import com.shopelia.android.widget.form.EditTextField;
import com.shopelia.android.widget.form.EditTextField.OnValidateListener;
import com.shopelia.android.widget.form.EmailField;
import com.shopelia.android.widget.form.FormField;
import com.shopelia.android.widget.form.FormField.ListenerAdapter;
import com.shopelia.android.widget.form.FormLinearLayout;
import com.shopelia.android.widget.form.HeaderField;
import com.shopelia.android.widget.form.PaymentCardField;
import com.shopelia.android.widget.form.PhoneField;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class SignUpFragment extends ShopeliaFragment<OnSignUpListener> {

    public interface OnSignUpListener {
        public void onSignUp(JSONObject result);

        public void requestSignIn(Bundle arguments);

        public ValidationButton getValidationButton();
    }

    public static final String FRAGMENT_NAME = "SignUp";

    private static final SparseArray<String> EVENTS;

    static {
        EVENTS = new SparseArray<String>();
        EVENTS.put(R.id.email, Analytics.Properties.Steps.SigningUp.EMAIL);
        EVENTS.put(R.id.phone, Analytics.Properties.Steps.SigningUp.PHONE);
        EVENTS.put(R.id.address, Analytics.Properties.Steps.SigningUp.ADDRESS);
        EVENTS.put(R.id.payment_card, Analytics.Properties.Steps.SigningUp.PAYMENT_CARD);
    }

    private FormLinearLayout mFormContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState != null) {
            mFormContainer.onCreate(savedInstanceState);
        } else {
            fireScreenSeenEvent(FRAGMENT_NAME);
            track(Analytics.Events.Steps.SignUp.BEGIN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_signup_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFormContainer = findViewById(R.id.form);

        //@formatter:off
        /*
         * User informations
         */
        mFormContainer.findFieldById(R.id.phone, PhoneField.class).setJsonPath(Order.Api.ADDRESS, Address.Api.PHONE).mandatory().setOnValidateListener(mPhoneOnValidateListener).setListener(mTrackingListener);
        mFormContainer.findFieldById(R.id.email, EmailField.class).setJsonPath(Order.Api.USER, User.Api.EMAIL).mandatory().setOnValidateListener(mEmailOnValidateListener).setListener(mTrackingListener);
        
        /*
         * Shipment details
         */
        mFormContainer.findFieldById(R.id.address, AddressField.class).setJsonPath(Order.Api.ADDRESS).setListener(mTrackingListener);
        
        /*
         * Payment methods
         */
        mFormContainer.findFieldById(R.id.header_payment_method, HeaderField.class).addPictures(R.drawable.shopelia_logos_visa, R.drawable.shopelia_logos_mc, R.drawable.shopelia_logos_norton);
        mFormContainer.findFieldById(R.id.payment_card, PaymentCardField.class).setJsonPath(Order.Api.PAYMENT_CARD).setListener(mTrackingListener);
        
        mFormContainer.onCreate(savedInstanceState);
        //@formatter:on

        getContract().getValidationButton().setOnClickListener(mOnClickListener);
    }

    public void onCreateAccountError(JSONObject object) {
        if (getActivity() == null) {
            return;
        }
        JSONArray names = object.names();
        final int count = names.length();
        for (int index = 0; index < count; index++) {
            String name = names.optString(index);
            JSONArray errors = object.optJSONArray(name);
            FormField field = mFormContainer.findFieldByPath(Order.Api.USER, User.Api.EMAIL);
            if (field != null) {
                field.setError(errors.optString(0));
            } else {
                Toast.makeText(getActivity(), errors.optString(0), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreateShopeliaActionBar(ActionBar actionBar) {
        super.onCreateShopeliaActionBar(actionBar);
        actionBar.clear();
        actionBar.addItem(new TextButtonItem(R.id.shopelia_action_bar_login, getActivity(), R.string.shopelia_action_bar_sign_in));
        actionBar.commit();
    }

    @Override
    protected void onActionItemSelected(Item item) {
        super.onActionItemSelected(item);
        if (item.getId() == R.id.shopelia_action_bar_login) {
            getContract().requestSignIn(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFormContainer.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFormContainer != null) {
            mFormContainer.onSaveInstanceState(outState);
        }
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (mFormContainer.validate()) {
                JSONObject result = mFormContainer.toJson();
                getContract().onSignUp(result);
            }
        }
    };

    private OnValidateListener mPhoneOnValidateListener = new OnValidateListener() {

        private String mCurrentNumber = "";

        @Override
        public boolean onValidate(EditTextField editTextField, boolean shouldFireError) {
            return true;
        }

        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            final String number = s.toString().replace(" ", "");
            if (PhoneField.PHONE_PATTERN.matcher(number).matches() && !number.equals(mCurrentNumber)) {
                mCurrentNumber = number;
                ShopeliaRestClient.get(Command.V1.Phones.Lookup(mCurrentNumber), null, new AsyncCallback() {

                    @Override
                    public void onComplete(HttpResponse httpResponse) {
                        try {
                            JSONObject address = new JSONObject(httpResponse.getBodyAsString());
                            AddressField field = (AddressField) mFormContainer.findFieldById(R.id.address);
                            if (field != null) {
                                Address addressWithPhone = Address.inflate(address);
                                addressWithPhone.phone = number;
                                field.setAddress(addressWithPhone);
                            }
                        } catch (JSONException e) {
                            onError(e);
                        }
                    }

                    public void onError(Exception e) {
                        mCurrentNumber = "";
                        e.printStackTrace();
                    };

                });
            }
        }

    };

    private OnValidateListener mEmailOnValidateListener = new OnValidateListener() {

        private HashSet<String> mCheckedEmails = new HashSet<String>();

        @Override
        public boolean onValidate(EditTextField editTextField, boolean shouldFireError) {
            return true;
        }

        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            if (true)
                return;
            final EmailField emailField = mFormContainer.findFieldById(R.id.email);
            if (emailField.onValidation(false)) {
                final String email = (String) emailField.getResult();
                if (mCheckedEmails.contains(email)) {
                    return;
                }
                mCheckedEmails.add(email);
                JSONObject params = new JSONObject();
                try {
                    params.put(User.Api.EMAIL, email);
                } catch (JSONException e) {

                }
                ShopeliaRestClient.post(Command.V1.Users.Exists(), params, new AsyncCallback() {

                    @Override
                    public void onComplete(HttpResponse httpResponse) {
                        if (httpResponse.getStatus() == 204 && getActivity() != null) {
                            Bundle arguments = new Bundle();
                            arguments.putString(SignInFragment.ARGS_EMAIL, email);
                            getContract().requestSignIn(arguments);
                            emailField.setError(getString(R.string.shopelia_error_user_already_exists, email));
                            Toast.makeText(getActivity(), getString(R.string.shopelia_error_user_already_exists, email), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    public void onError(Exception e) {
                        e.printStackTrace();
                    };

                });
            }
        }

    };

    private FormField.ListenerAdapter mTrackingListener = new ListenerAdapter() {

        @Override
        public void onValidChanged(FormField field) {
            String event = EVENTS.get(field.getId());
            if (event != null && field.isValid() && field.getResult() != null) {
                track(Analytics.Events.Steps.SignUp.SIGNING_UP, AnalyticsBuilder.prepareStepPackage(getActivity(), event));
            }
        };

    };

}
