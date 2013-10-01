package com.shopelia.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.app.CardFragment;

public class ProductSelectionCardFragment extends CardFragment {

    public static final String TAG = "Product Selection";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_product_summary_card, container, false);
    }

    @Override
    public void onBindView(View view, Bundle savedInstanceState) {
        super.onBindView(view, savedInstanceState);
    }

    private static TextView clear(TextView tv) {
        tv.setText(null);
        return tv;
    }

}
