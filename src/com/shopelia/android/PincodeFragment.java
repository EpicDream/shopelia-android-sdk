package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.shopelia.android.PincodeFragment.PincodeHandler;
import com.shopelia.android.PincodeFragment.PincodeHandler.Callback;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.concurent.ScheduledTask;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.widget.Errorable;
import com.shopelia.android.widget.NumberInput;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.TextButtonItem;

public class PincodeFragment extends ShopeliaFragment<PincodeHandler> {

    public interface PincodeHandler {

        public interface Callback {
            public void onPincodeCheckDone(boolean succeed);
        }

        public String PREFERENCE_NAME = "ShopeliaPincodeHandler";

        public boolean isCreatingPincode();

        public boolean isUpdatingPincode();

        public boolean sendPincode(String pincode);

        public boolean isServiceAvailable();

        public long getUnlockDate();

        public void setPincodeCallback(PincodeHandler.Callback callback);

    }

    public static final String ARGS_STEP = "args:step";
    public static final String ARGS_ERROR_MESSAGE = "args:error_message";

    public static final int STEP_CREATION = 0;
    public static final int STEP_VERIFICATION = 1;
    public static final int STEP_CONFIRMATION = 2;

    private static final long REFRESH_PERIOD = 1000L;

    private TextView mHeaderTitle;
    private NumberInput mNumberInput;
    private TextView mErrorMessage;

    private ScheduledTask mRefreshTask = new ScheduledTask();

    public static PincodeFragment newInstance(int step, String error) {
        Bundle arguments = new Bundle();
        arguments.putInt(ARGS_STEP, step);
        arguments.putString(ARGS_ERROR_MESSAGE, error);
        PincodeFragment fragment = new PincodeFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContract().setPincodeCallback(mCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_pincode_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View headerFrame = view.findViewById(R.id.header_frame);
        headerFrame.findViewById(R.id.lock).setVisibility(View.GONE);
        ImageView icon = (ImageView) headerFrame.findViewById(R.id.icon);
        mHeaderTitle = (TextView) headerFrame.findViewById(R.id.title);

        icon.setImageResource(R.drawable.shopelia_lock);
        switch (getArguments().getInt(ARGS_STEP)) {
            case STEP_CREATION:
                mHeaderTitle.setText(R.string.shopelia_pincode_create);
                break;
            case STEP_CONFIRMATION:
                mHeaderTitle.setText(R.string.shopelia_pincode_re_enter);
                break;
            case STEP_VERIFICATION:
                mHeaderTitle.setText(R.string.shopelia_pincode_enter);
                break;
        }
        mNumberInput = (NumberInput) view.findViewById(R.id.numberInput);
        mNumberInput.requestFocus();
        mNumberInput.addTextChangedListener(mTextWatcher);
        mErrorMessage = (TextView) view.findViewById(R.id.error);
        if (getArguments().containsKey(ARGS_ERROR_MESSAGE)) {
            setError(getArguments().getString(ARGS_ERROR_MESSAGE));
        }

        mNumberInput.setEnabled(getContract().isServiceAvailable());
        if (!getContract().isServiceAvailable()) {
            mRefreshTask.scheduleAtFixedRate(mRefreshUiRunnable, getUnlockDelay() % 1000, REFRESH_PERIOD);
            mRefreshUiRunnable.run();
        } else {
            requestNumberInputFocus();
        }

        TextView forgotten = findViewById(R.id.forgotPincode);
        forgotten.setText(Html.fromHtml(forgotten.getText().toString()));
        forgotten.setOnClickListener(mOnForgottenClickListener);
        forgotten.setVisibility(getContract().isCreatingPincode() || getContract().isUpdatingPincode() ? View.GONE : View.VISIBLE);
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
            UserManager.get(getActivity()).logout();
            getActivity().setResult(ShopeliaActivity.RESULT_LOGOUT);
            getActivity().finish();
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            setError(null);
            mNumberInput.setError(false);
            checkPincode();
        }
    };

    private void setError(String message) {
        setError(message, true);
    }

    private void setError(String message, boolean shake) {
        mErrorMessage.setText(message);
        if (message == null) {
            mErrorMessage.setVisibility(View.INVISIBLE);
        } else {
            mErrorMessage.setVisibility(View.VISIBLE);
            if (getActivity() != null && shake) {
                mNumberInput.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shopelia_wakeup));
            }
        }
    }

    private void checkPincode() {
        String pincode = mNumberInput.getText().toString();
        if (pincode.length() == 4) {
            getContract().sendPincode(mNumberInput.getText().toString());
            mNumberInput.setEnabled(false);
        }

    }

    private long getUnlockDelay() {
        return getContract().getUnlockDate() - System.currentTimeMillis();
    }

    private void requestNumberInputFocus() {
        mNumberInput.requestFocus();
        (new Handler()).postDelayed(new Runnable() {

            public void run() {
                MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0,
                        0, 0);
                mNumberInput.dispatchTouchEvent(event);
                event.recycle();
                event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0);
                mNumberInput.dispatchTouchEvent(event);
                event.recycle();
            }
        }, 200);
    }

    private Runnable mRefreshUiRunnable = new Runnable() {

        @Override
        public void run() {
            if (getActivity() == null) {
                return;
            }

            if (getContract().isServiceAvailable()) {
                mNumberInput.setEnabled(true);
                mNumberInput.setError(false);
                requestNumberInputFocus();
                setError(null);
                mRefreshTask.stop();
            } else {
                mNumberInput.setEnabled(false);
                mNumberInput.clearFocus();
                String errorMessage;
                long delay = getUnlockDelay();
                if (delay >= 60 * 60 * 1000) {
                    errorMessage = getString(R.string.shopelia_pincode_retry,
                            getResources().getQuantityString(R.plurals.hour, (int) delay / (60 * 60 * 1000), delay / (60 * 60 * 1000)));
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

    private PincodeHandler.Callback mCallback = new Callback() {

        @Override
        public void onPincodeCheckDone(boolean succeed) {
            if (!succeed) {
                mNumberInput.setText(null);
                mNumberInput.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shopelia_wakeup));
                requestNumberInputFocus();
            }
            ((Errorable) mNumberInput).setError(!succeed);
            if (((Errorable) mNumberInput).hasError() && getActivity() != null) {
                setError(getResources().getString(R.string.shopelia_pincode_wrong));
                mNumberInput.removeTextChangedListener(mTextWatcher);
                mNumberInput.setText(null);
                mNumberInput.addTextChangedListener(mTextWatcher);
                mNumberInput.requestFocus();
            }
            mNumberInput.setEnabled(getContract().isServiceAvailable());

            if (!getContract().isServiceAvailable()) {
                mRefreshTask.scheduleAtFixedRate(mRefreshUiRunnable, getUnlockDelay() % 1000, REFRESH_PERIOD);
            }
        }
    };

    private OnClickListener mOnForgottenClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), RecoverPincodeActivity.class);
            getActivity().startActivityForResult(intent, PincodeActivity.REQUEST_RECOVER_PINCODE);
        }
    };

}
