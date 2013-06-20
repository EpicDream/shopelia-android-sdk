package com.shopelia.android.widget;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.R;
import com.shopelia.android.model.Merchant;
import com.shopelia.android.model.Product;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.Tax;

public class ProductSheetWrapper {

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

    public ProductSheetWrapper(View view) {
        mRootView = view;
        mProductName = findViewById(R.id.product_name);
        mProductDescription = findViewById(R.id.product_description);
        mProductShippingInfo = findViewById(R.id.product_shipping_info);
        mShippingFees = findViewById(R.id.product_shipping_fees);
        mProductImage = findViewById(R.id.product_image);
        mProductPrice = findViewById(R.id.product_price);
        mVendorLogo = findViewById(R.id.product_vendor_icon);
        mVendorText = findViewById(R.id.product_vendor_text);
        mTax = findViewById(R.id.product_tax);
    }

    public ProductSheetWrapper(View view, Bundle args) {
        this(view);
        setArguments(args);
        refreshView();
    }

    public ProductSheetWrapper setArguments(Bundle args) {
        mArguments = args;
        return this;
    }

    public ProductSheetWrapper setProductInfo(Product product) {
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

    public ProductSheetWrapper noBackground() {
        mHasBackground = false;
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
        mVendorLogo.setUrl(vendor.logo);
        if (!mHasBackground) {
            mRootView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(int id) {
        if (mRootView == null) {
            return null;
        }
        return (T) mRootView.findViewById(id);
    }

    public String getString(int id) {
        return mRootView.getContext().getString(id);
    }

    public String getString(int id, Object... args) {
        return mRootView.getContext().getString(id, args);
    }

}
