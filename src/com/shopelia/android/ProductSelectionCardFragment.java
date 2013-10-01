package com.shopelia.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.app.CardFragment;
import com.shopelia.android.model.Product;
import com.shopelia.android.model.Version;

public class ProductSelectionCardFragment extends CardFragment {

    public static final String TAG = "Product Selection";

    private static final String ARGS_PRODUCT = "args:product";

    private Product mProduct;

    public static ProductSelectionCardFragment newInstance(Product product) {
        ProductSelectionCardFragment fragment = new ProductSelectionCardFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_product_selection_card, container, false);
    }

    @Override
    public void onBindView(View view, Bundle savedInstanceState) {
        super.onBindView(view, savedInstanceState);
        mProduct = getArguments().getParcelable(ARGS_PRODUCT);
        refreshPrices();
    }

    private void refreshPrices() {
        setPriceOrHide(R.id.product_price, mProduct.getCurrentVersion().productPrice);
        setPriceOrHide(R.id.delivery_price, mProduct.getCurrentVersion().shippingPrice);
        setPriceOrHide(R.id.product_total_price, mProduct.getCurrentVersion().getTotalPrice());
        setMinusPriceOrHide(R.id.price_cashfront, mProduct.getCurrentVersion().cashfrontValue);
        findViewById(R.id.product_delivery_free_layout).setVisibility(
                mProduct.getCurrentVersion().shippingPrice <= 0.f ? View.VISIBLE : View.GONE);

    }

    private void setPriceOrHide(int id, float price) {
        setPriceOrHide(id, price, Version.NO_PRICE);
    }

    private void setPriceOrHide(int id, float price, float failureValue) {
        TextView textView = findViewById(id);
        textView.setText(mProduct.currency.format(price));
        if (price == failureValue) {
            ((View) textView.getParent()).setVisibility(View.GONE);
        } else {
            ((View) textView.getParent()).setVisibility(View.VISIBLE);
        }
    }

    private void setMinusPriceOrHide(int id, float price) {
        TextView textView = findViewById(id);
        textView.setText("-" + mProduct.currency.format(price));
        if (price <= 0.f) {
            ((View) textView.getParent()).setVisibility(View.GONE);
        } else {
            ((View) textView.getParent()).setVisibility(View.VISIBLE);
        }
    }

}
