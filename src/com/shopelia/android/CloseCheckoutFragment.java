package com.shopelia.android;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.analytics.AnalyticsBuilder;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.utils.DialogHelper;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.TextButtonItem;

public class CloseCheckoutFragment extends ShopeliaFragment<Void> {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_close_checkout_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.validate).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                track(Analytics.Events.Steps.Finalize.END, AnalyticsBuilder.prepareMethodPackage(getActivity(),
                        Analytics.Properties.Steps.Finalizing.Method.BACK_ON_APPLICATION));
                getActivity().finish();
            }
        });
        initPhoneLayout();
    }

    private void initPhoneLayout() {
        TextView phone = (TextView) findViewById(R.id.call_shopelia);
        phone.setText(Html.fromHtml(phone.getText().toString()));
        phone.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:01.82.09.15.44"));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException e) {

                }
            }
        });

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
                    track(Analytics.Events.Steps.Finalize.END,
                            AnalyticsBuilder.prepareMethodPackage(getActivity(), Analytics.Properties.Steps.Finalizing.Method.SIGN_OUT));
                    UserManager.get(getActivity()).logout();
                    getActivity().setResult(ShopeliaActivity.RESULT_LOGOUT);
                    getActivity().finish();
                }
            }, null).create().show();

        }
    }

}
