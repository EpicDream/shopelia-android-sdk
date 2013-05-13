package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.shopelia.android.PincodeFragment.PincodeHandler;
import com.shopelia.android.app.HostActivity;
import com.shopelia.android.config.Config;

public class PincodeActivity extends HostActivity implements PincodeHandler {

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

    /**
     * The max number of try before locking the application
     */
    public static final String EXTRA_NUMBER_OF_TRY = Config.EXTRA_PREFIX + "NUMBER_OF_TRY";

    private boolean mCreatePincode = true;
    private String mPincode = null;
    private int mMaxTry = 5;
    private int mAttemptNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.shopelia_process_order_activity);

        init(savedInstanceState != null ? savedInstanceState : getIntent().getExtras());

        if (savedInstanceState == null) {
            handleFragment();
        }
    }

    private void init(Bundle bundle) {
        if (bundle != null) {
            mCreatePincode = bundle.getBoolean(EXTRA_CREATE_PINCODE);
            if (bundle.containsKey(EXTRA_PINCODE)) {
                mPincode = bundle.get(EXTRA_PINCODE).toString();
            }
        }
        mCreatePincode = TextUtils.isEmpty(mPincode);
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_CREATE_PINCODE, mCreatePincode);
        outState.putString(EXTRA_PINCODE, mPincode);
        outState.putInt(EXTRA_NUMBER_OF_TRY, mMaxTry);
    }

    @Override
    public boolean isCreatingPincode() {
        return mCreatePincode;
    }

    @Override
    public String getPincode() {
        return mPincode;
    }

    @Override
    public boolean sendPincode(String pincode) {
        boolean hadPincode = mPincode != null;
        if (isCreatingPincode() && mPincode == null) {
            mPincode = pincode;
        }
        if (pincode.equals(mPincode)) {
            if (!isCreatingPincode() || hadPincode) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_PINCODE, pincode);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                handleFragment();
            }
            return true;
        } else {
            if (isCreatingPincode() && hadPincode) {
                mPincode = null;
                handleFragment();
            } else {
                mAttemptNumber++;
            }
            return false;
        }
    }

    @Override
    public void reset() {
        if (isCreatingPincode()) {
            mPincode = null;
        }
    }

    private void handleFragment() {
        int step = isCreatingPincode() ? PincodeFragment.STEP_CREATION : PincodeFragment.STEP_VERIFICATION;
        if (step == PincodeFragment.STEP_CREATION && !TextUtils.isEmpty(mPincode)) {
            step = PincodeFragment.STEP_CONFIRMATION;
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, PincodeFragment.newInstance(step));
        ft.commit();
    }

}
