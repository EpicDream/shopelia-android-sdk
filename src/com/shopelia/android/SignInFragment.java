package com.shopelia.android;

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.shopelia.android.SignInFragment.OnSignInListener;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.widget.ValidationButton;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.TextButtonItem;
import com.shopelia.android.widget.form.FormLinearLayout;

public class SignInFragment extends ShopeliaFragment<OnSignInListener> {

    public interface OnSignInListener {
        public void onSignIn(JSONObject result);

        public void requestSignUp();

        public ValidationButton getValidationButton();
    }

    private FormLinearLayout mFormContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_signin_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFormContainer = findViewById(R.id.form);
        getContract().getValidationButton().setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onCreateShopeliaActionBar(ActionBar actionBar) {
        super.onCreateShopeliaActionBar(actionBar);
        actionBar.clear();
        actionBar.addItem(new TextButtonItem(R.id.shopelia_action_bar_create_account, getActivity(), R.string.shopelia_action_bar_sign_up));
        actionBar.commit();
    }

    @Override
    protected void onActionItemSelected(Item item) {
        super.onActionItemSelected(item);
        if (item.getId() == R.id.shopelia_action_bar_create_account) {
            getContract().requestSignUp();
        }
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (mFormContainer.validate()) {
                JSONObject result = mFormContainer.toJson();
                getContract().onSignIn(result);
            }
        }
    };

}
