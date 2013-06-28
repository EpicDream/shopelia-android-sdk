package com.shopelia.android;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.shopelia.android.WelcomeFragment.WelcomeParent;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.model.Merchant;

public class WelcomeFragment extends ShopeliaFragment<WelcomeParent> {

    public interface WelcomeParent {
        public void continueWithShopelia();

        public void continueWithMerchant();

        public Merchant getMerchant();

        public void cancel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_welcome_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup container = findViewById(R.id.list);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        String[] list = getResources().getStringArray(R.array.shopelia_welcome_list);
        for (String item : list) {
            addItemInViewGroup(inflater, container, item);
        }

        findViewById(R.id.continue_with_shopelia).setOnClickListener(mOnContinueWithShopeliaClickListener);
        findViewById(R.id.continue_with_merchant).setOnClickListener(mOnContinueWithMerchantClickListener);
        findViewById(R.id.outside_area).setOnClickListener(mOnOutsideAreaClickListener);
        View frame = findViewById(R.id.frame);
        frame.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
        frame.setVisibility(View.VISIBLE);
        frame.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shopelia_pop_up));

        findViewById(R.id.continue_with_merchant_site, TextView.class).setText(
                Html.fromHtml(getResources().getString(R.string.shopelia_welcome_continue_on, getContract().getMerchant().name)));

    }

    protected void addItemInViewGroup(LayoutInflater inflater, ViewGroup container, String item) {
        View v = inflater.inflate(R.layout.shopelia_welcome_list_item, container, false);
        TextView textView = (TextView) v.findViewById(android.R.id.text1);
        textView.setText(Html.fromHtml(item));
        container.addView(v, container.getChildCount());
    }

    private OnClickListener mOnContinueWithShopeliaClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            getContract().continueWithShopelia();
        }
    };

    private OnClickListener mOnContinueWithMerchantClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            getContract().continueWithMerchant();
        }
    };

    private OnClickListener mOnOutsideAreaClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            getContract().cancel();
        }
    };

}
