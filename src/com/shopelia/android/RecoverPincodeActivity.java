package com.shopelia.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.shopelia.android.concurent.ScheduledTask;
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

    public static final String ACTIVITY_NAME = "Recover Pincode";

    private static final int REQUEST_CREATE_PINCODE = 0x906;

    private FormLinearLayout mFormContainer;
    private TextView mErrorMessage;

    private long mTimeToBlock = 0L;

    private static final long REFRESH_PERIOD = 1000L;
    private NumberField mLastNumbers;
    private NumberField mExpiryDate;

    private ScheduledTask mRefreshTask = new ScheduledTask();

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_recover_pincode_activity);
        mTimeToBlock = getSharedPreferences().getLong(PincodeActivity.PREF_TIME_TO_BLOCK, 0L);
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

        mExpiryDate = mFormContainer.findFieldById(R.id.expiryDate);
        mLastNumbers = mFormContainer.findFieldById(R.id.lastNumbers);

        requestFocus();
        if (!isServiceAvailable()) {
            mRefreshTask.scheduleAtFixedRate(mRefreshUiRunnable, getUnlockDate() % 1000, REFRESH_PERIOD);
            mRefreshUiRunnable.run();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_PINCODE) {
            setResult(resultCode);
            finish();
        }
    }

    private SharedPreferences getSharedPreferences() {
        return getSharedPreferences(PincodeActivity.PREFERENCE_NAME, Context.MODE_PRIVATE);
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

    public void forbidOrder(long delay) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        mTimeToBlock = System.currentTimeMillis() + delay;
        editor.putLong(PincodeActivity.PREF_TIME_TO_BLOCK, mTimeToBlock);
        editor.commit();
    }

    public boolean isServiceAvailable() {
        return mTimeToBlock < System.currentTimeMillis();
    }

    public long getUnlockDate() {
        return mTimeToBlock;
    }

    public long getUnlockDelay() {
        return mTimeToBlock - System.currentTimeMillis();
    }

    private Runnable mRefreshUiRunnable = new Runnable() {

        @Override
        public void run() {

            if (isServiceAvailable()) {
                mExpiryDate.setEnabled(true);
                mExpiryDate.setError(false);

                requestFocus();

                mLastNumbers.setEnabled(true);
                mLastNumbers.setError(false);
                findViewById(R.id.validate).setEnabled(true);
                setError(null, false);
                mRefreshTask.stop();
            } else {
                findViewById(R.id.validate).setEnabled(false);
                mLastNumbers.setEnabled(false);
                mLastNumbers.clearFocus();
                mExpiryDate.setEnabled(false);
                mExpiryDate.clearFocus();
                mExpiryDate.setContentText(null);
                mLastNumbers.setContentText(null);
                String errorMessage;
                long delay = getUnlockDelay();
                if (delay >= 60 * 60 * 1000) {
                    errorMessage = getString(
                            R.string.shopelia_pincode_retry,
                            getResources().getQuantityString(R.plurals.hour, (int) delay / (60 * 60 * 1000), delay / (60 * 60 * 1000)),
                            getResources().getQuantityString(R.plurals.second, (int) (delay % (60 * 60 * 1000)) / (1000),
                                    (delay % (60 * 60 * 1000)) / (1000)));
                } else if (delay >= 60 * 1000) {
                    errorMessage = getString(
                            R.string.shopelia_pincode_retry,
                            getResources().getQuantityString(R.plurals.minute, (int) delay / (60 * 1000), delay / (60 * 1000)),
                            getResources().getQuantityString(R.plurals.second, (int) (delay % (60 * 1000)) / (1000),
                                    (delay % (60 * 1000)) / (1000)));
                } else if (delay >= 1000) {
                    errorMessage = getString(R.string.shopelia_pincode_retry_seconds,
                            getResources().getQuantityString(R.plurals.second, (int) delay / (1000), delay / (1000)));
                } else {
                    errorMessage = null;
                }
                setError(errorMessage, false);
            }
        }
    };

    private OnClickListener mOnClickValidateListener = new OnClickListener() {

        @Override
        public void onClick(final View v) {
            if (mFormContainer.validate()) {
                setWaitingMode(true);
                v.setEnabled(false);
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
                            v.setEnabled(true);
                            if (httpResponse.getStatus() == 204) {
                                Intent intent = new Intent(RecoverPincodeActivity.this, PincodeActivity.class);
                                intent.putExtra(PincodeActivity.EXTRA_CREATE_PINCODE, true);
                                intent.putExtra(PincodeActivity.EXTRA_UPDATE_PINCODE, true);
                                startActivityForResult(intent, REQUEST_CREATE_PINCODE);
                            } else {
                                setError(getString(R.string.shopelia_recover_pin_error), true);
                                if (!isServiceAvailable()) {
                                    mRefreshTask.scheduleAtFixedRate(mRefreshUiRunnable, getUnlockDate() % 1000, REFRESH_PERIOD);
                                }
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            setError(getString(R.string.shopelia_recover_pin_network_error), false);
                            v.setEnabled(true);
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
