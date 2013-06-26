package com.shopelia.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.app.ShopeliaFragment;

public class WelcomeFragment extends ShopeliaFragment<Void> {

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
    }

    protected void addItemInViewGroup(LayoutInflater inflater, ViewGroup container, String item) {
        View v = inflater.inflate(R.layout.shopelia_welcome_list_item, container, false);
        TextView textView = (TextView) v.findViewById(android.R.id.text1);
        textView.setText(item);
        container.addView(v, container.getChildCount());
    }

}
