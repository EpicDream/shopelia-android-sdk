package com.shopelia.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shopelia.android.app.CardFragment;

public class ProductSummaryCardFragment extends CardFragment {

    public static final String TAG = "ProductSummary";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_product_summary_card, container, false);
    }

}
