package com.shopelia.android;

import java.util.Timer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.PincodeFragment.PincodeHandler;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class PincodeActivity extends ShopeliaActivity implements PincodeHandler {

    public static final String ACTIVITY_NAME = "Pincode";

    /**
     * An optional boolean value. If this field is not set and
     * {@link PincodeActivity#EXTRA_CHECK_PINCODE} exists, this will be false,
     * true otherwise
     */
    public static final String EXTRA_CREATE_PINCODE = Config.EXTRA_PREFIX + "CREATE_PINCODE";

    /**
     * A {@link String} or {@link Integer} representing the pin code to match
     */
    public static final String EXTRA_PINCODE = Config.EXTRA_PREFIX + "PINCODE";

    private static final long MIN_ERROR_DELAY = 5 * 60 * 1000;
    private static final long MAX_ERROR_DELAY = 1 * 60 * 60 * 1000;

    private static final String PREF_TIME_TO_BLOCK = "SPH_LAST_ATTEMPT";
    private static final String PREF_FAILURE_COUNT = "SPH_FAILURE_COUNT";
    private static final String PREF_ATTEMPTS = "SPH_ATTEMPTS";

    private boolean mCreatePincode = true;
    private long mTimeToBlock;

    private Timer mTimer = new Timer();

    private PincodeHandler.Callback mPincodeHandlerCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.shopelia_process_order_activity);

        mTimeToBlock = getSharedPreferences().getLong(PREF_TIME_TO_BLOCK, 0L);

        init(savedInstanceState != null ? savedInstanceState : getIntent().getExtras());

        if (savedInstanceState == null) {
            handleFragment(null);
        }

        if (mTimeToBlock + MAX_ERROR_DELAY < System.currentTimeMillis()) {
            releaseOrder();
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

    @Override
    public boolean sendPincode(final String pincode) {
        if (!isServiceAvailable()) {
            return false;
        }
        if (isCreatingPincode()) {

            Intent data = new Intent();
            data.putExtra(EXTRA_CREATE_PINCODE, true);
            data.putExtra(EXTRA_PINCODE, pincode);
            setResult(RESULT_OK, data);
            finish();
            return true;
        }
        ShopeliaRestClient.authenticate(this);

        JSONObject params = new JSONObject();
        try {
            params.put(User.Api.PINCODE, pincode);
        } catch (JSONException e) {
        }
        ShopeliaRestClient.post(Command.V1.Users.Verify.$, params, mAsyncCallback);
        return true;
    }

    public void forbidOrder() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        mTimeToBlock = System.currentTimeMillis() + MIN_ERROR_DELAY;
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
        return ACTIVITY_NAME;
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
                if (mPincodeHandlerCallback != null) {
                    mPincodeHandlerCallback.onPincodeCheckDone(false);
                }
            }
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        };

    };

}
