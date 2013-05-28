package com.shopelia.android;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.shopelia.android.SignInFragment.OnSignInListener;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.User;
import com.shopelia.android.widget.ValidationButton;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.TextButtonItem;
import com.shopelia.android.widget.form.EditTextField;
import com.shopelia.android.widget.form.EmailField;
import com.shopelia.android.widget.form.FormLinearLayout;
import com.shopelia.android.widget.form.PasswordField;

public class SignInFragment extends ShopeliaFragment<OnSignInListener> {

    public interface OnSignInListener {
        public void onSignIn(JSONObject result);

        public void requestSignUp();

        public ValidationButton getValidationButton();
    }

    public static final String ARGS_EMAIL = "args:email";

    private static final int REQUEST_EMAIL = 0x1010;

    private FormLinearLayout mFormContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_signin_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFormContainer = findViewById(R.id.form);
        //@formatter:off
        /*
         * User informations
         */
        mFormContainer.findFieldById(R.id.email, EmailField.class).setJsonPath(Order.Api.USER, User.Api.EMAIL).mandatory();
        mFormContainer.findFieldById(R.id.password, PasswordField.class).setJsonPath(Order.Api.USER, User.Api.PASSWORD).mandatory();

        mFormContainer.onCreate(savedInstanceState);
        //@formatter:on

        TextView recoverPassword = findViewById(R.id.forgotPassword);
        recoverPassword.setText(Html.fromHtml(recoverPassword.getText().toString()));
        recoverPassword.setOnClickListener(mOnRecoverPasswordClick);

        getContract().getValidationButton().setOnClickListener(mOnClickListener);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() != null) {
            if (getArguments().containsKey(ARGS_EMAIL)) {
                mFormContainer.findFieldById(R.id.email, EditTextField.class).setContentText(getArguments().getString(ARGS_EMAIL));
                mFormContainer.nextField(mFormContainer.findFieldById(R.id.email));
            }
        }
    }

    @Override
    protected void onCreateShopeliaActionBar(ActionBar actionBar) {
        super.onCreateShopeliaActionBar(actionBar);
        actionBar.clear();
        actionBar.addItem(new TextButtonItem(R.id.shopelia_action_bar_create_account, getActivity(), R.string.shopelia_action_bar_sign_up));
        actionBar.commit();
    }

    @Override
    protected void onActionItemSelected(Item item) {
        super.onActionItemSelected(item);
        if (item.getId() == R.id.shopelia_action_bar_create_account) {
            getContract().requestSignUp();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EMAIL && resultCode == Activity.RESULT_OK) {
            mFormContainer.findFieldById(R.id.email, EmailField.class).setContentText(
                    data.getStringExtra(RecoverPasswordActivity.EXTRA_EMAIL));
            mFormContainer.findFieldById(R.id.email, EmailField.class).invalidate();
            return;
        }
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
                getContract().onSignIn(result);
            }
        }
    };

    private OnClickListener mOnRecoverPasswordClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), RecoverPasswordActivity.class);
            intent.putExtra(RecoverPasswordActivity.EXTRA_EMAIL, (String) findViewById(R.id.email, EmailField.class).getResult());
            startActivityForResult(intent, REQUEST_EMAIL);
        }
    };

}
