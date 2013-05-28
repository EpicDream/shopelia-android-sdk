package com.shopelia.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.shopelia.android.SignUpFragment.OnSignUpListener;
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
import com.shopelia.android.widget.form.FormLinearLayout;
import com.shopelia.android.widget.form.HeaderField;
import com.shopelia.android.widget.form.PaymentCardField;
import com.shopelia.android.widget.form.PhoneField;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class SignUpFragment extends ShopeliaFragment<OnSignUpListener> {

    public interface OnSignUpListener {
        public void onSignUp(JSONObject result);

        public void requestSignIn();

        public ValidationButton getValidationButton();
    }

    private FormLinearLayout mFormContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState != null) {
            mFormContainer.onCreate(savedInstanceState);
            ;
        }
        Log.d(null, "Load STATE " + savedInstanceState);
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
        mFormContainer.findFieldById(R.id.phone, PhoneField.class).setJsonPath(Order.Api.USER, User.Api.PHONE).mandatory().setOnValidateListener(mPhoneOnValidateListener);
        mFormContainer.findFieldById(R.id.email, EmailField.class).setJsonPath(Order.Api.USER, User.Api.EMAIL).mandatory().setOnValidateListener(mEmailOnValidateListener);
        
        /*
         * Shipment details
         */
        mFormContainer.findFieldById(R.id.address, AddressField.class).setJsonPath(Order.Api.ADDRESS);
        
        /*
         * Payment methods
         */
        mFormContainer.findFieldById(R.id.header_payment_method, HeaderField.class).addPictures(R.drawable.shopelia_logos_visa, R.drawable.shopelia_logos_mc, R.drawable.shopelia_logos_norton);
        mFormContainer.findFieldById(R.id.payment_card, PaymentCardField.class).setJsonPath(Order.Api.PAYMENT_CARD);
        
        mFormContainer.onCreate(savedInstanceState);
        //@formatter:on

        getContract().getValidationButton().setOnClickListener(mOnClickListener);
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
            getContract().requestSignIn();
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
        Log.d(null, "SAVE STATE " + outState);
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
            String number = s.toString().replace(" ", "");
            if (PhoneField.PHONE_PATTERN.matcher(number).matches() && !number.equals(mCurrentNumber)) {
                mCurrentNumber = number;
                ShopeliaRestClient.get(Command.V1.Phones.Lookup(mCurrentNumber), null, new AsyncCallback() {

                    @Override
                    public void onComplete(HttpResponse httpResponse) {
                        try {
                            JSONObject address = new JSONObject(httpResponse.getBodyAsString());
                            AddressField field = (AddressField) mFormContainer.findFieldById(R.id.address);
                            if (field != null) {
                                field.setAddress(Address.inflate(address));
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

        @Override
        public boolean onValidate(EditTextField editTextField, boolean shouldFireError) {
            return true;
        }

        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            final EmailField emailField = mFormContainer.findFieldById(R.id.email);
            if (emailField.onValidation(false)) {
                final String email = (String) emailField.getResult();
                JSONObject params = new JSONObject();
                try {
                    params.put(User.Api.EMAIL, email);
                } catch (JSONException e) {

                }
                ShopeliaRestClient.post(Command.V1.Users.Exists(), params, new AsyncCallback() {

                    @Override
                    public void onComplete(HttpResponse httpResponse) {
                        if (httpResponse.getStatus() == 204 && getActivity() != null) {
                            emailField.setError(true);
                            emailField.getView().startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shopelia_wakeup));
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

}
