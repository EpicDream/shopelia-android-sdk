package com.shopelia.android;

import java.util.Timer;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.shopelia.android.PincodeFragment.PincodeHandler;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class PincodeActivity extends ShopeliaActivity implements PincodeHandler {

    public static final String PREFIX_CREATE = "Create";
    public static final String PREFIX_EDIT = "Edit";
    public static final String PREFIX_VERIFY = "Verify";
    public static final String ACTIVITY_NAME = " Pincode";

    /**
     * An optional boolean value. If this field is not set and
     * {@link PincodeActivity#EXTRA_CHECK_PINCODE} exists, this will be false,
     * true otherwise
     */
    public static final String EXTRA_CREATE_PINCODE = Config.EXTRA_PREFIX + "CREATE_PINCODE";

    public static final String EXTRA_UPDATE_PINCODE = Config.EXTRA_PREFIX + "UPDATE_PINCODE";

    /**
     * A {@link String} or {@link Integer} representing the pin code to match
     */
    public static final String EXTRA_PINCODE = Config.EXTRA_PREFIX + "PINCODE";

    public static final int REQUEST_RECOVER_PINCODE = 0x7000;

    static final long SECONDS = 1000;

    static final String PREF_TIME_TO_BLOCK = "SPH_TIME_TO_BLOCK";
    static final String JSON_DELAY = "delay";

    private boolean mCreatePincode = true;
    private boolean mUpdatePincode = false;
    private long mTimeToBlock;

    private Timer mTimer = new Timer();

    private PincodeHandler.Callback mPincodeHandlerCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.shopelia_process_order_activity);
        mTimeToBlock = getSharedPreferences().getLong(PREF_TIME_TO_BLOCK, 0L);

        init(savedInstanceState != null ? savedInstanceState : getIntent().getExtras());

        if (mCreatePincode || mUpdatePincode) {
            mTimeToBlock = 0L;
        }

        mUpdatePincode = getIntent().getBooleanExtra(EXTRA_UPDATE_PINCODE, false);
        if (savedInstanceState == null) {
            handleFragment(null);
        }

        if (mTimeToBlock < System.currentTimeMillis()) {
            releaseOrder();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RECOVER_PINCODE && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private SharedPreferences getSharedPreferences() {
        return getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private void init(Bundle bundle) {
        if (bundle != null) {
            mCreatePincode = bundle.getBoolean(EXTRA_CREATE_PINCODE);
        }
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_CREATE_PINCODE, mCreatePincode);
    }

    @Override
    public boolean isCreatingPincode() {
        return mCreatePincode;
    }

    public void onPincodeCreated(final String pincode) {
        createConfirmationDialog(pincode, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent data = new Intent();
                data.putExtra(EXTRA_CREATE_PINCODE, true);
                data.putExtra(EXTRA_PINCODE, pincode);
                setResult(RESULT_OK, data);
                finish();
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public boolean sendPincode(final String pincode) {
        if (!isServiceAvailable()) {
            return false;
        }
        if (isCreatingPincode() && !mUpdatePincode) {
            onPincodeCreated(pincode);
            return true;
        }

        if (isCreatingPincode() && mUpdatePincode) {
            JSONObject object = new JSONObject();
            try {
                JSONObject userObject = new JSONObject();
                userObject.put(User.Api.PINCODE, pincode);
                object.put(User.Api.USER, userObject);
            } catch (JSONException e) {

            }

            ShopeliaRestClient.authenticate(this);
            ShopeliaRestClient.put(Command.V1.Users.User(UserManager.get(this).getUser().id), object, new AsyncCallback() {

                @Override
                public void onComplete(HttpResponse httpResponse) {
                    if (httpResponse.getStatus() == 204) {
                        onPincodeCreated(pincode);

                    }
                }

                @Override
                public void onError(Exception e) {
                    super.onError(e);
                    Toast.makeText(PincodeActivity.this, R.string.shopelia_error_network_error, Toast.LENGTH_LONG).show();
                }

            });
            return true;
        }

        ShopeliaRestClient.authenticate(this);

        JSONObject params = new JSONObject();
        try {
            params.put(User.Api.PINCODE, pincode);
        } catch (JSONException e) {
        }
        startDelayedWaiting(getString(R.string.shopelia_pincode_verification), false, false, 500L);
        ShopeliaRestClient.post(Command.V1.Users.Verify.$, params, mAsyncCallback);
        return true;
    }

    public void forbidOrder(long delay) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        mTimeToBlock = System.currentTimeMillis() + delay;
        editor.putLong(PREF_TIME_TO_BLOCK, mTimeToBlock);
        editor.commit();
    }

    public void releaseOrder() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.commit();
    }

    private void handleFragment(String errorMessage) {
        int step = isCreatingPincode() ? PincodeFragment.STEP_CREATION : PincodeFragment.STEP_VERIFICATION;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, PincodeFragment.newInstance(step, errorMessage));
        ft.commit();
    }

    @Override
    public String getActivityName() {
        String prefix = PREFIX_VERIFY;
        if (getIntent().getBooleanExtra(EXTRA_UPDATE_PINCODE, false)) {
            prefix = PREFIX_EDIT;
        } else if (getIntent().getBooleanExtra(EXTRA_CREATE_PINCODE, false)) {
            prefix = PREFIX_CREATE;
        }
        return prefix + ACTIVITY_NAME;
    }

    @Override
    public boolean isServiceAvailable() {
        return mTimeToBlock < System.currentTimeMillis();
    }

    @Override
    public long getUnlockDate() {
        return mTimeToBlock;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        mTimer = null;
    }

    @Override
    public void setPincodeCallback(Callback callback) {
        mPincodeHandlerCallback = callback;
    }

    private AlertDialog createConfirmationDialog(String pincode, final OnClickListener listener) {
        //@formatter:off
        return new AlertDialog.Builder(this)
            .setTitle(R.string.shopelia_dialog_title)
            .setMessage(getString(R.string.shopelia_pincode_your_pincode_is, pincode))
            .setPositiveButton(android.R.string.ok, listener)
            .setOnCancelListener(new OnCancelListener() {
                
                @Override
                public void onCancel(DialogInterface dialog) {
                    listener.onClick(dialog, 0);
                }
            })
            .create();
        //@formater:on
    }

    private AsyncCallback mAsyncCallback = new AsyncCallback() {

        @Override
        public void onComplete(HttpResponse response) {
            if (response.getStatus() == 204) {
                releaseOrder();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                if (mPincodeHandlerCallback != null) {
                    mPincodeHandlerCallback.onPincodeCheckDone(true);
                }
            } else {
                stopWaiting();
                if (response.getStatus() == 503) {
                    try {
                        Log.d(null, response.getBodyAsString());
                        JSONObject object = new JSONObject(response.getBodyAsString());
                        forbidOrder(object.optLong(JSON_DELAY, 300) * SECONDS);
                    } catch (JSONException e) {

                    }
                } else if (response.getStatus() == 500) {
                    if (mPincodeHandlerCallback != null) {
                        mPincodeHandlerCallback.resetPincode();
                    }
                    return ;
                }
                if (mPincodeHandlerCallback != null) {
                    mPincodeHandlerCallback.onPincodeCheckDone(false);
                }
            }
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
            Toast.makeText(PincodeActivity.this, R.string.shopelia_error_network_error, Toast.LENGTH_LONG).show();
            stopWaiting();
            if (mPincodeHandlerCallback != null) {
                mPincodeHandlerCallback.resetPincode();
            }
        };

    };

    @Override
    public boolean isUpdatingPincode() {
        return mUpdatePincode;
    }

}
