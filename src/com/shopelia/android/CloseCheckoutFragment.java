package com.shopelia.android;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.shopelia.android.CloseCheckoutFragment.OnCreatePasswordListener;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.utils.ContextUtils;
import com.shopelia.android.utils.DialogHelper;
import com.shopelia.android.widget.FontableTextView;
import com.shopelia.android.widget.ValidationButton;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.TextButtonItem;

public class CloseCheckoutFragment extends ShopeliaFragment<OnCreatePasswordListener> {

    public interface OnCreatePasswordListener {
        public void onCreatePassword();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_close_checkout_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ValidationButton validate = findViewById(R.id.validate);
        FontableTextView exit = findViewById(R.id.exit);
        User user = UserManager.get(getActivity()).getUser();
        if (user.has_password == 0) {
            exit.setText(getString(R.string.shopelia_close_checkout_validation_or_exit,
                    ContextUtils.getApplicationName(getActivity(), getActivity().getString(R.string.shopelia_default_app_name))));
            validate.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    getContract().onCreatePassword();
                }
            });
            exit.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        } else {
            validate.setText(getString(R.string.shopelia_close_checkout_validation,
                    ContextUtils.getApplicationName(getActivity(), getActivity().getString(R.string.shopelia_default_app_name))));
            view.findViewById(R.id.validate).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
            exit.setVisibility(View.GONE);
        }

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
            DialogHelper.buildLogoutDialog(getActivity(), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    closeSoftKeyboard();
                    UserManager.get(getActivity()).logout();
                    getActivity().setResult(ShopeliaActivity.RESULT_LOGOUT);
                    getActivity().finish();
                }
            }, null).show();

        }
    }

}
