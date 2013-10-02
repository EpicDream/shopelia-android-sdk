package com.shopelia.android;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.app.CardFragment;
import com.shopelia.android.model.Product;
import com.shopelia.android.widget.AsyncImageView;

public class ProductSummaryCardFragment extends CardFragment {

    public static final String TAG = "ProductSummary";

    private Product mProduct;
    private TextView mProductTitle;
    private TextView mProductDescription;
    private AsyncImageView mProductImage;
    private TextView mMerchantName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_product_summary_card, container, false);
    }

    @Override
    public void onBindView(View view, Bundle savedInstanceState) {
        super.onBindView(view, savedInstanceState);
        clear(mProductTitle = findViewById(R.id.product_title));
        clear(mProductDescription = findViewById(R.id.product_description));
        clear(mMerchantName = findViewById(R.id.product_merchant_name));
        mProductImage = findViewById(R.id.product_image);

        mMerchantName.setOnClickListener(mOnClickOnMerchantListener);
        findViewById(R.id.product_more).setOnClickListener(mOnClickOnMoreListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivityEventBus().registerSticky(this, Product.class);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivityEventBus().unregister(this);
    }

    // Events
    public void onEventMainThread(Product product) {
        mProduct = product;
        mProductTitle.setText(product.getCurrentVersion().name);
        mProductDescription.setText(Html.fromHtml(product.getCurrentVersion().description));
        mProductImage.setUrl(product.getCurrentVersion().imageUrl);
        mMerchantName.setText(product.merchant.name);
    }

    public void onEventMainThread(ProductOptionsFragment.OnOptionsChanged event) {

    }

    private static TextView clear(TextView tv) {
        tv.setText(null);
        return tv;
    }

    private OnClickListener mOnClickOnMerchantListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

        }
    };

    private OnClickListener mOnClickOnMoreListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

        }
    };

}
