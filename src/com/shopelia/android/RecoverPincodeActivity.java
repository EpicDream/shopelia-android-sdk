package com.shopelia.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.shopelia.android.widget.form.EditTextField;
import com.shopelia.android.widget.form.EditTextField.OnValidateListener;
import com.shopelia.android.widget.form.FormLinearLayout;
import com.shopelia.android.widget.form.NumberField;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class RecoverPincodeActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "RecoverPincode";

    private static final int REQUEST_CREATE_PINCODE = 0x906;

    private FormLinearLayout mFormContainer;
    private TextView mErrorMessage;

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_recover_pincode_activity);
        mFormContainer = (FormLinearLayout) findViewById(R.id.form);
        mFormContainer.findFieldById(R.id.lastNumbers, NumberField.class).setMinLength(4).setJsonPath(User.Api.CC_NUMBER).mandatory()
                .setOnValidateListener(new OnValidateListener() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        super.onTextChanged(s, start, before, count);
                        if (start + count == 4 && before < count) {
                            mFormContainer.nextField(mFormContainer.findFieldById(R.id.lastNumbers));
                        }
                    }

                    @Override
                    public boolean onValidate(EditTextField editTextField, boolean shouldFireError) {
                        return true;
                    }
                });
        mFormContainer.findFieldById(R.id.expiryDate, NumberField.class).setMinLength(5).setJsonPath(User.Api.CC_MONTH).mandatory()
                .setOnValidateListener(new OnValidateListener() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        super.onTextChanged(s, start, before, count);
                        if (count + start == 5 && before < count) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(mFormContainer.getWindowToken(), 0);
                        }
                    }

                    @Override
                    public boolean onValidate(EditTextField editTextField, boolean shouldFireError) {
                        return true;
                    }
                });
        mFormContainer.onCreate(saveState);
        mFormContainer.findFieldById(R.id.lastNumbers, NumberField.class).requestFocus();
        findViewById(R.id.validate).setOnClickListener(mOnClickValidateListener);
        mErrorMessage = (TextView) findViewById(R.id.error);
        requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_PINCODE) {
            setResult(resultCode);
            finish();
        }
    }

    private void setError(String message, boolean shake) {
        mErrorMessage.setText(message);
        if (message == null) {
            mErrorMessage.setVisibility(View.GONE);
        } else {
            mErrorMessage.setVisibility(View.VISIBLE);
        }
        if (shake) {
            mFormContainer.findFieldById(R.id.lastNumbers, NumberField.class).startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.shopelia_wakeup));
            mFormContainer.findFieldById(R.id.lastNumbers, NumberField.class).setError(true);
            mFormContainer.findFieldById(R.id.expiryDate, NumberField.class).startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.shopelia_wakeup));
            mFormContainer.findFieldById(R.id.expiryDate, NumberField.class).setError(true);
        }
    }

    private void requestFocus() {
        final EditTextField field = (EditTextField) findViewById(R.id.lastNumbers);

        if (field != null) {
            field.requestFocus();
            (new Handler()).postDelayed(new Runnable() {

                public void run() {
                    MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN,
                            0, 0, 0);
                    field.getView().dispatchTouchEvent(event);
                    event.recycle();
                    event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0);
                    field.getView().dispatchTouchEvent(event);
                    event.recycle();
                }
            }, 400);
        }
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

    private OnClickListener mOnClickValidateListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mFormContainer.validate()) {
                setWaitingMode(true);
                JSONObject object = mFormContainer.toJson();
                try {
                    String month = object.getString(User.Api.CC_MONTH);
                    String year = month.substring(3);
                    object.put(User.Api.CC_MONTH, month.substring(0, 2));
                    object.put(User.Api.CC_YEAR, year);
                    ShopeliaRestClient.authenticate(RecoverPincodeActivity.this);
                    ShopeliaRestClient.post(Command.V1.Users.Verify.$, object, new AsyncCallback() {

                        @Override
                        public void onComplete(HttpResponse httpResponse) {
                            if (httpResponse.getStatus() == 204) {
                                Intent intent = new Intent(RecoverPincodeActivity.this, PincodeActivity.class);
                                intent.putExtra(PincodeActivity.EXTRA_CREATE_PINCODE, true);
                                intent.putExtra(PincodeActivity.EXTRA_UPDATE_PINCODE, true);
                                startActivityForResult(intent, REQUEST_CREATE_PINCODE);
                            } else {
                                setError(getString(R.string.shopelia_recover_pin_error), true);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            setError(getString(R.string.shopelia_recover_pin_network_error), false);
                        };

                    });
                } catch (JSONException e) {
                    // Do nothing
                }
            } else {
                setError(null, true);
            }
        }
    };

}
