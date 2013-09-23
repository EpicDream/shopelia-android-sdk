package com.shopelia.android;

import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiController;
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.shopelia.android.widget.form.EditTextField;
import com.shopelia.android.widget.form.EditTextField.OnValidateListener;
import com.shopelia.android.widget.form.FormLinearLayout;
import com.shopelia.android.widget.form.PasswordField;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class CreatePasswordFragment extends ShopeliaFragment<Void> {

    public static final String TAG = "CreatePasswordFragment";

    private FormLinearLayout mFormContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_form_create_password, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFormContainer = findViewById(R.id.form);
        mFormContainer.findFieldById(R.id.password, PasswordField.class).setJsonPath(User.Api.USER, User.Api.PASSWORD)
                .setOnValidateListener(new OnValidateListener() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mFormContainer.findFieldById(R.id.password).clearErrorsCache();
                        mFormContainer.findFieldById(R.id.confirm).clearErrorsCache();
                    }

                    @Override
                    public boolean onValidate(EditTextField editTextField, boolean shouldFireError) {
                        boolean has_error = false;
                        String p1 = editTextField.getResultAsString();
                        String p2 = mFormContainer.findFieldById(R.id.confirm, EditTextField.class).getResultAsString();

                        has_error = !(p1 != null && p1.length() >= 6);

                        if (has_error && shouldFireError) {
                            editTextField.setError(getString(R.string.shopelia_form_password_min_length));
                        }
                        return !has_error;
                    }
                });
        mFormContainer.findFieldById(R.id.confirm, PasswordField.class).setJsonPath(User.Api.USER, User.Api.PASSWORD_CONFIRMATION)
                .setOnValidateListener(new OnValidateListener() {

                    @Override
                    public boolean onValidate(EditTextField editTextField, boolean shouldFireError) {
                        boolean has_error = false;
                        String p1 = mFormContainer.findFieldById(R.id.password, EditTextField.class).getResultAsString();
                        String p2 = editTextField.getResultAsString();

                        has_error = !(p1 != null && p1.length() >= 6);

                        if (!has_error) {
                            has_error = !(p1 != null && p2 != null && p1.equals(p2));
                            if (has_error && shouldFireError) {
                                editTextField.setError(getString(R.string.shopelia_form_password_no_match));
                            }
                        }
                        return !has_error;
                    }

                });
        findViewById(R.id.validate).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mFormContainer.validate()) {
                    User user = UserManager.get(getActivity()).getUser();
                    startWaiting(getString(R.string.shopelia_form_password_waiting), true, false);
                    JSONObject object = mFormContainer.toJson();
                    ShopeliaRestClient.V1(getActivity()).put(Command.V1.Users.User(user.id), object, new AsyncCallback() {

                        @Override
                        public void onComplete(HttpResponse httpResponse) {
                            stopWaiting();
                            if (httpResponse.getStatus() == 204) {
                                getActivity().finish();
                            } else {
                                mFormContainer.findFieldById(R.id.password, PasswordField.class).setError(
                                        ApiController.ErrorInflater.grabErrorMessage(httpResponse.getBodyAsString()));
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            stopWaiting();
                            Toast.makeText(getActivity(), R.string.shopelia_error_network_error, Toast.LENGTH_LONG).show();
                        }

                    });
                }
            }
        });
        mFormContainer.onCreate(savedInstanceState);
    }

}
