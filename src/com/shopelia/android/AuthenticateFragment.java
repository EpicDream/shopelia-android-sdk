package com.shopelia.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.analytics.AnalyticsBuilder;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiHandler;
import com.shopelia.android.remote.api.VerifyAPI;
import com.shopelia.android.utils.DialogHelper;
import com.shopelia.android.widget.FontableTextView;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.TextButtonItem;
import com.shopelia.android.widget.form.EditTextField;
import com.shopelia.android.widget.form.EditTextField.OnValidateListener;
import com.shopelia.android.widget.form.FormContainer;
import com.shopelia.android.widget.form.FormContainer.OnSubmitListener;
import com.shopelia.android.widget.form.FormLinearLayout;
import com.shopelia.android.widget.form.PasswordField;
import com.turbomanage.httpclient.HttpResponse;

public class AuthenticateFragment extends ShopeliaFragment<Void> {

    private FormLinearLayout mFormContainer;
    private VerifyAPI mVerifyAPI;
    private FontableTextView mErrorMessage;
    private PasswordField mPasswordField;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_authenticate_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User user = UserManager.get(getActivity()).getUser();
        ((TextView) view.findViewById(R.id.email)).setText(user.email);
        view.findViewById(R.id.validate).setOnClickListener(mOnValidateClickListener);
        view.findViewById(R.id.forgotPassword).setOnClickListener(mOnRecoverPasswordClickListener);

        mFormContainer = findViewById(R.id.form);
        mFormContainer.findFieldById(R.id.password, PasswordField.class).setJsonPath(User.Api.PASSWORD);
        mFormContainer.setOnSubmitListener(mOnSubmitListener);
        mFormContainer.onCreate(savedInstanceState);

        mPasswordField = mFormContainer.findFieldById(R.id.password);

        findViewById(R.id.remember_me, CheckBox.class).setOnCheckedChangeListener(mOnCheckedChangeListener);

        mErrorMessage = findViewById(R.id.error);

        mVerifyAPI = new VerifyAPI(getActivity(), mApiCallback);
    }

    @Override
    protected void onCreateShopeliaActionBar(ActionBar actionBar) {
        super.onCreateShopeliaActionBar(actionBar);
        actionBar.clear();
        actionBar.addItem(new TextButtonItem(R.id.shopelia_action_bar_sign_out, getActivity(), R.string.shopelia_action_bar_sign_out));
        actionBar.commit();
    }

    @Override
    protected void onActionItemSelected(Item item) {
        super.onActionItemSelected(item);
        if (item.getId() == R.id.shopelia_action_bar_sign_out) {
            DialogHelper.buildLogoutDialog(getActivity(), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    closeSoftKeyboard();
                    track(Analytics.Events.Steps.Finalize.END,
                            AnalyticsBuilder.prepareMethodPackage(getActivity(), Analytics.Properties.Steps.Finalizing.Method.SIGN_OUT));
                    UserManager.get(getActivity()).logout();
                    getActivity().startActivityForResult(new Intent(getActivity(), PrepareOrderActivity.class), 12);
                }
            }, null).create().show();

        }
    }

    private OnClickListener mOnRecoverPasswordClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), RecoverPasswordActivity.class);
            intent.putExtra(RecoverPasswordActivity.EXTRA_EMAIL, (String) findViewById(R.id.email, TextView.class).getText().toString());
            startActivity(intent);
        }
    };

    private OnClickListener mOnValidateClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            mOnSubmitListener.onSubmit(mFormContainer);
        }
    };

    private OnSubmitListener mOnSubmitListener = new OnSubmitListener() {

        @Override
        public void onSubmit(FormContainer container) {
            if (mFormContainer.validate()) {
                if (mVerifyAPI.verify(mFormContainer.toJson())) {
                    mErrorMessage.setVisibility(View.GONE);
                    mPasswordField.setEnabled(false);
                    startWaiting("Authentification...", false, false);
                }
            }
        }
    };

    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            View v = findViewById(R.id.disclaimer);
            v.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }
    };

    private ApiHandler.CallbackAdapter mApiCallback = new ApiHandler.CallbackAdapter() {

        @Override
        public void onVerifySucceed() {
            mPasswordField.setValid(true);
            getActivity().startActivityForResult(new Intent(getActivity(), PrepareOrderActivity.class), 12);
        };

        @Override
        public void onVerifyFailed() {
            mPasswordField.setEnabled(true);
            mErrorMessage.setVisibility(View.VISIBLE);
            mErrorMessage.setText(R.string.shopelia_authenticate_wrong_password);
            mPasswordField.setError(true);
            mPasswordField.setContentText("");
            mFormContainer.findFieldById(R.id.password).startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shopelia_wakeup));
            mPasswordField.setOnValidateListener(new OnValidateListener() {

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mPasswordField.setOnValidateListener(null);
                    mPasswordField.setError(false);
                    mErrorMessage.setVisibility(View.GONE);
                };

                @Override
                public boolean onValidate(EditTextField editTextField, boolean shouldFireError) {
                    return true;
                }
            });
            stopWaiting();
        };

        @Override
        public void onVerifyUpdateUI(VerifyAPI api, boolean locked, long delay, String message) {
            stopWaiting();
            mPasswordField.setError(false);
            if (locked) {
                mPasswordField.setEnabled(false);
                mErrorMessage.setVisibility(View.VISIBLE);
                mErrorMessage.setText(message);
            } else {
                mErrorMessage.setVisibility(View.GONE);
                mPasswordField.setEnabled(true);
            }
        };

        @Override
        public void onError(int step, HttpResponse httpResponse, org.json.JSONObject response, Exception e) {
            mErrorMessage.setVisibility(View.VISIBLE);
            mErrorMessage.setText(R.string.shopelia_error_network_error);
            stopWaiting();
        };

    };

}
