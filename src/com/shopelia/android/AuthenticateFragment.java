package com.shopelia.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.analytics.AnalyticsBuilder;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.shopelia.android.utils.DialogHelper;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.TextButtonItem;
import com.shopelia.android.widget.form.FormContainer;
import com.shopelia.android.widget.form.FormContainer.OnSubmitListener;
import com.shopelia.android.widget.form.FormLinearLayout;
import com.shopelia.android.widget.form.PasswordField;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class AuthenticateFragment extends ShopeliaFragment<Void> {

    private FormLinearLayout mFormContainer;

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

        findViewById(R.id.remember_me, CheckBox.class).setOnCheckedChangeListener(mOnCheckedChangeListener);

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
                ShopeliaRestClient.authenticate(getActivity());
                ShopeliaRestClient.post(Command.V1.Users.Verify.$, mFormContainer.toJson(), new AsyncCallback() {

                    @Override
                    public void onComplete(HttpResponse httpResponse) {
                        if (httpResponse.getStatus() == 204) {
                            getActivity().startActivityForResult(new Intent(getActivity(), PrepareOrderActivity.class), 12);
                        }
                        onDone();
                    }

                    @Override
                    public void onError(Exception e) {
                        onDone();
                    }

                    public void onDone() {
                        stopWaiting();
                    }

                });
                startWaiting("Authentification...", false, false);
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

}
