package com.shopelia.android;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.model.Option;
import com.shopelia.android.model.Options;
import com.shopelia.android.model.Product;

public class ProductOptionsFragment extends ShopeliaFragment<Void> {

    public static final String TAG = "ProductOptions";

    private List<OptionsItem> mOptionsItems;
    private ViewGroup mOptionsContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_product_options_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOptionsContainer = findViewById(R.id.options_container);
        Product product = (Product) getActivityEventBus().getStickyEvent(Product.class);
        refreshUi(product);
    }

    private void refreshUi(Product product) {
        if (mOptionsItems == null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final int size = product.versions.getOptionsCount();
            mOptionsItems = new ArrayList<ProductOptionsFragment.OptionsItem>(product.versions.getOptionsCount());
            mOptionsContainer.removeAllViews();
            for (int index = 0; index < size; index++) {
                OptionsItem oi = new OptionsItem(index);
                mOptionsItems.add(oi);
                View v = oi.inflate(inflater, mOptionsContainer);
                oi.attachView(v);
                mOptionsContainer.addView(v);
            }
        }
        final int size = product.versions.getOptionsCount();
        for (int index = 0; index < size; index++) {
            mOptionsItems.get(index).refreshUi(product.versions.getOptions(index));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivityEventBus().registerSticky(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivityEventBus().unregister(this);
    }

    private class OptionsItem {

        private int mIndex = 0;
        private View mView;

        private Spinner mSelector;
        private TextView mOptionLabel;

        public OptionsItem(int index) {
            mIndex = index;
        }

        public View inflate(LayoutInflater inflater, ViewGroup container) {
            return inflater.inflate(R.layout.shopelia_product_option_item_fragment, container, false);
        }

        public void attachView(View v) {
            mView = v;
            mSelector = findViewById(R.id.options_selector);
            mOptionLabel = findViewById(R.id.option_label);
        }

        public <T extends View> T findViewById(int id) {
            return (T) mView.findViewById(id);
        }

        public void refreshUi(Options options) {
            ArrayAdapter<Option> adapter = new ArrayAdapter<Option>(mView.getContext(), android.R.layout.simple_dropdown_item_1line);
            for (Option option : options) {
                adapter.add(option);
            }
            mSelector.setAdapter(adapter);
            mOptionLabel.setText(getResources().getString(R.string.shopelia_product_options_option_pattern, (mIndex + 1)));
        }

    }

    // Events

    public void onEventMainThread(Product product) {
        refreshUi(product);
    }

    // Spinners

}
