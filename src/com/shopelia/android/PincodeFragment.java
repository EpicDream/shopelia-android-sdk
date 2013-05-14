package com.shopelia.android;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.shopelia.android.PincodeFragment.PincodeHandler;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.widget.Errorable;
import com.shopelia.android.widget.NumberInput;

public class PincodeFragment extends ShopeliaFragment<PincodeHandler> {

    public interface PincodeHandler {
        public boolean isCreatingPincode();

        public String getPincode();

        public boolean sendPincode(String pincode);

        public void reset();
    }

    public static final String ARGS_STEP = "args:step";
    public static final String ARGS_ERROR_MESSAGE = "args:error_message";

    public static final int STEP_CREATION = 0;
    public static final int STEP_VERIFICATION = 1;
    public static final int STEP_CONFIRMATION = 2;

    private TextView mHeaderTitle;
    private NumberInput mNumberInput;
    private TextView mErrorMessage;

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
        mNumberInput.setOnEditorActionListener(mOnEditorActionListener);
        mNumberInput.requestFocus();
        mNumberInput.addTextChangedListener(mTextWatcher);
        mErrorMessage = (TextView) view.findViewById(R.id.error);
        if (getArguments().containsKey(ARGS_ERROR_MESSAGE)) {
            setError(getArguments().getString(ARGS_ERROR_MESSAGE));
        }
    }

    private OnEditorActionListener mOnEditorActionListener = new OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            String pincode = v.getText().toString();
            if (pincode.length() == 4 && TextUtils.isDigitsOnly(pincode)) {
                ((Errorable) v).setError(!getContract().sendPincode(v.getText().toString()));
                if (((Errorable) v).hasError() && getActivity() != null) {
                    setError(getResources().getString(R.string.shopelia_pincode_wrong));
                }
            } else {
                ((Errorable) v).setError(true);
            }
            return true;
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mNumberInput.setError(false);
        }
    };

    private void setError(String message) {
        mErrorMessage.setText(message);
        if (message == null) {
            mErrorMessage.setVisibility(View.GONE);
        } else {
            mErrorMessage.setVisibility(View.VISIBLE);
        }
    }

}
