package com.shopelia.android.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.R;
import com.shopelia.android.model.Merchant;
import com.shopelia.android.model.Product;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.Tax;
import com.shopelia.android.widget.AsyncImageView.OnAsyncImageViewLoadListener;

public class ProductSheetWidget extends FrameLayout {

    private View mRootView;
    private Bundle mArguments;

    // Views
    private FontableTextView mProductName;
    private FontableTextView mProductDescription;
    private FontableTextView mProductShippingInfo;
    private FontableTextView mShippingFees;
    private AsyncImageView mProductImage;
    @SuppressWarnings("unused")
    private FontableTextView mVendorText;
    private AsyncImageView mVendorLogo;
    private FontableTextView mProductPrice;
    private FontableTextView mTax;

    private boolean mHasBackground = true;

    public ProductSheetWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (mRootView == null) {
            mRootView = onCreateView(LayoutInflater.from(context));
            onViewCreated();
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                if (activity.getIntent().getExtras() != null) {
                    setArguments(activity.getIntent().getExtras());
                }
            }
        }
    }

    public View onCreateView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.shopelia_product_sheet, this, true);
    }

    public void onViewCreated() {
        mProductName = (FontableTextView) findViewById(R.id.product_name);
        mProductDescription = (FontableTextView) findViewById(R.id.product_description);
        mProductShippingInfo = (FontableTextView) findViewById(R.id.product_shipping_info);
        mShippingFees = (FontableTextView) findViewById(R.id.product_shipping_fees);
        mProductImage = (AsyncImageView) findViewById(R.id.product_image);
        mProductPrice = (FontableTextView) findViewById(R.id.product_price);
        mVendorLogo = (AsyncImageView) findViewById(R.id.product_vendor_icon);
        mVendorText = (FontableTextView) findViewById(R.id.product_vendor_text);
        mTax = (FontableTextView) findViewById(R.id.product_tax);
    }

    public ProductSheetWidget setArguments(Bundle args) {
        mArguments = args;
        refreshView();
        return this;
    }

    public ProductSheetWidget setProductInfo(Product product) {
        Bundle args = new Bundle();
        args.putString(PrepareOrderActivity.EXTRA_PRODUCT_TITLE, product.name);
        args.putString(PrepareOrderActivity.EXTRA_PRODUCT_DESCRIPTION, product.description);
        args.putParcelable(PrepareOrderActivity.EXTRA_PRODUCT_IMAGE, product.image);
        args.putFloat(PrepareOrderActivity.EXTRA_PRICE, product.productPrice);
        args.putFloat(PrepareOrderActivity.EXTRA_SHIPPING_PRICE, product.deliveryPrice);
        args.putString(PrepareOrderActivity.EXTRA_SHIPPING_INFO, product.shippingExtra);
        args.putParcelable(PrepareOrderActivity.EXTRA_TAX, product.tax);
        args.putParcelable(PrepareOrderActivity.EXTRA_CURRENCY, product.currency);
        args.putParcelable(PrepareOrderActivity.EXTRA_MERCHANT, product.merchant);
        setArguments(args);
        return this;
    }

    public void refreshView() {
        if (mRootView == null || mArguments == null) {
            return;
        }
        Currency currency = Currency.EUR;
        Tax tax = Tax.ATI;
        Merchant vendor = null;
        if (mArguments.containsKey(PrepareOrderActivity.EXTRA_CURRENCY)) {
            currency = mArguments.getParcelable(PrepareOrderActivity.EXTRA_CURRENCY);
        }

        if (mArguments.containsKey(PrepareOrderActivity.EXTRA_MERCHANT)) {
            vendor = mArguments.getParcelable(PrepareOrderActivity.EXTRA_MERCHANT);
        }

        if (mArguments.containsKey(PrepareOrderActivity.EXTRA_TAX)) {
            tax = mArguments.getParcelable(PrepareOrderActivity.EXTRA_TAX);
        }
        if (mArguments.containsKey(PrepareOrderActivity.EXTRA_SHIPPING_PRICE)) {
            final float shippingPrice = mArguments.getFloat(PrepareOrderActivity.EXTRA_SHIPPING_PRICE);
            if (shippingPrice > 0) {
                mShippingFees.setText(getString(R.string.shopelia_product_shipping_fees, currency.format(shippingPrice)));
            } else {
                mShippingFees.setText(getString(R.string.shopelia_product_free_shipping));
            }
            mShippingFees.setVisibility(View.VISIBLE);
        } else {
            mShippingFees.setVisibility(View.GONE);
        }

        mProductName.setText(mArguments.getString(PrepareOrderActivity.EXTRA_PRODUCT_TITLE));
        mProductDescription.setText(mArguments.getString(PrepareOrderActivity.EXTRA_PRODUCT_DESCRIPTION));
        mProductDescription.setVisibility(!TextUtils.isEmpty(mProductDescription.getText()) ? View.VISIBLE : View.GONE);
        mProductPrice.setText(currency.format(mArguments.getFloat(PrepareOrderActivity.EXTRA_PRICE)));
        mProductShippingInfo.setText(mArguments.getString(PrepareOrderActivity.EXTRA_SHIPPING_INFO));
        mProductShippingInfo.setVisibility(mArguments.containsKey(PrepareOrderActivity.EXTRA_SHIPPING_INFO) ? View.VISIBLE : View.GONE);
        mTax.setText(tax.getResId());
        Object image = mArguments.get(PrepareOrderActivity.EXTRA_PRODUCT_IMAGE);
        if (image != null && image instanceof Uri) {
            mProductImage.setImageURI((Uri) image);
        }
        if (vendor != null) {
            mVendorLogo.setUrl(vendor.logo);
            mVendorLogo.setOnAsyncImageViewLoadListener(new OnAsyncImageViewLoadListener() {

                @Override
                public void onLoadingStarted(AsyncImageView imageView) {
                }

                @Override
                public void onLoadingFailed(AsyncImageView imageView, Exception exception) {
                    postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            mVendorLogo.stopLoading();
                            mVendorLogo.forceDownload();
                            mVendorLogo.reload();
                        }
                    }, 500);
                }

                @Override
                public void onLoadingEnded(AsyncImageView imageView, Bitmap image) {
                }
            });
        }
    }

    public String getString(int id) {
        return mRootView.getContext().getString(id);
    }

    public String getString(int id, Object... args) {
        return mRootView.getContext().getString(id, args);
    }

}
