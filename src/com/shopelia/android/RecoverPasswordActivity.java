package com.shopelia.android;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.shopelia.android.widget.form.EmailField;
import com.shopelia.android.widget.form.FormLinearLayout;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class RecoverPasswordActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "Recover Password";

    public static final String EXTRA_EMAIL = Config.EXTRA_PREFIX + "EMAIL";

    private FormLinearLayout mFormLayout;
    private TextView mErrorMessage;
    private EmailField mEmailField;

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_recover_password_activity);
        findViewById(R.id.validate).setOnClickListener(mOnValidateClickListener);
        mFormLayout = (FormLinearLayout) findViewById(R.id.form);
        mFormLayout.findFieldById(R.id.email, EmailField.class).setJsonPath(User.Api.EMAIL).mandatory();
        mFormLayout.onCreate(saveState);
        mFormLayout.findFieldById(R.id.email, EmailField.class).setContentText(getIntent().getStringExtra(EXTRA_EMAIL));
        mEmailField = mFormLayout.findFieldById(R.id.email);
        if (TextUtils.isEmpty((CharSequence) mEmailField.getResult())) {
            requestFocus();
        }
        mEmailField.setValid(mEmailField.onValidation(false));
        mFormLayout.updateSections();
        mErrorMessage = (TextView) findViewById(R.id.error);
    }

    private void setError(String message, boolean shake) {
        mErrorMessage.setText(message);
        if (message == null) {
            mErrorMessage.setVisibility(View.GONE);
        } else {
            mErrorMessage.setVisibility(View.VISIBLE);
        }
        if (shake) {
            mEmailField.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shopelia_wakeup));
            mEmailField.setError(true);
        }
    }

    private void requestFocus() {
        if (mEmailField.requestFocus()) {
            return;
        }
        (new Handler()).postDelayed(new Runnable() {

            public void run() {
                if (mEmailField.getView() == null) {
                    return;
                }
                MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0,
                        0, 0);
                mEmailField.getView().dispatchTouchEvent(event);
                event.recycle();
                event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0);
                mEmailField.getView().dispatchTouchEvent(event);
                event.recycle();
            }
        }, 400);
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

    private OnClickListener mOnValidateClickListener = new OnClickListener() {

        @Override
        public void onClick(final View v) {
            if (mFormLayout.validate()) {
                JSONObject params = mFormLayout.toJson();
                v.setEnabled(false);
                ShopeliaRestClient.post(Command.V1.Users.Reset(), params, new AsyncCallback() {

                    @Override
                    public void onComplete(HttpResponse httpResponse) {
                        v.setEnabled(true);
                        if (httpResponse.getStatus() == 404) {
                            setError(getString(R.string.shopelia_recover_password_unknown_email), true);
                            requestFocus();
                        } else {
                            Intent data = new Intent();
                            data.putExtra(EXTRA_EMAIL, (String) mEmailField.getResult());
                            setResult(RESULT_OK, data);
                            finish();
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.shopelia_recover_password_email_sent,
                                            Toast.LENGTH_LONG).show();
                                }
                            }, 500);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        setError(getString(R.string.shopelia_recover_password_network_error), false);
                        v.setEnabled(true);
                    };

                });
            } else {
                setError(TextUtils.isEmpty(mErrorMessage.getText()) || TextUtils.isEmpty(mEmailField.getResultAsString()) ? null
                        : mErrorMessage.getText().toString(), true);
                requestFocus();
            }
        }
    };

}
