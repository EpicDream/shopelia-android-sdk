package com.shopelia.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.shopelia.android.widget.form.EditTextField;
import com.shopelia.android.widget.form.FormLinearLayout;
import com.shopelia.android.widget.form.NumberField;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class RecoverPincodeActivity extends ShopeliaActivity {

    public static final String ACTIVITY_NAME = "RecoverPincode";

    private FormLinearLayout mFormContainer;
    private TextView mErrorMessage;

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_recover_pincode_activity);
        mFormContainer = (FormLinearLayout) findViewById(R.id.form);
        mFormContainer.findFieldById(R.id.lastNumbers, NumberField.class).setMinLength(4).setJsonPath(User.Api.CC_NUMBER).mandatory();
        mFormContainer.findFieldById(R.id.expiryDate, NumberField.class).setMinLength(4).setJsonPath(User.Api.CC_MONTH).mandatory();
        mFormContainer.onCreate(saveState);

        findViewById(R.id.validate).setOnClickListener(mOnClickValidateListener);
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
            mFormContainer.findFieldById(R.id.lastNumbers, NumberField.class).startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.shopelia_wakeup));
            mFormContainer.findFieldById(R.id.lastNumbers, NumberField.class).setError(true);
            mFormContainer.findFieldById(R.id.expiryDate, NumberField.class).startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.shopelia_wakeup));
            mFormContainer.findFieldById(R.id.expiryDate, NumberField.class).setError(true);
        }
    }

    private void requestFocus() {
        final EditTextField field = null;
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
                JSONObject object = mFormContainer.toJson();
                try {
                    String month = object.getString(User.Api.CC_MONTH);
                    String year = month.substring(2);
                    object.put(User.Api.CC_MONTH, month.substring(0, 2));
                    object.put(User.Api.CC_YEAR, year);
                    ShopeliaRestClient.authenticate(RecoverPincodeActivity.this);
                    ShopeliaRestClient.post(Command.V1.Users.Verify.$, object, new AsyncCallback() {

                        @Override
                        public void onComplete(HttpResponse httpResponse) {
                            Log.d(null, httpResponse.getStatus() + "  RESPONSE " + httpResponse.getBodyAsString());
                            if (httpResponse.getStatus() == 204) {
                                setResult(RESULT_OK);
                                finish();
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
