package com.shopelia.android;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shopelia.android.app.ShopeliaFragment;
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

        public OptionsItem(int index) {
            mIndex = index;
        }

        public View inflate(LayoutInflater inflater, ViewGroup container) {
            return inflater.inflate(R.layout.shopelia_product_option_item_fragment, container, false);
        }

        public void attachView(View v) {
            mView = v;
        }

        public void refreshUi(Options options) {

        }

    }

    // Events

    public void onEventMainThread(Product product) {
        refreshUi(product);
    }

}
