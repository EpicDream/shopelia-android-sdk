package com.shopelia.android.widget;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.R;
import com.shopelia.android.model.Vendor;
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
    private ImageView mProductImage;
    @SuppressWarnings("unused")
    private FontableTextView mVendorText;
    private ImageView mVendorLogo;
    private FontableTextView mProductPrice;
    private FontableTextView mTax;

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

    public void setArguments(Bundle args) {
        mArguments = args;
    }

    public void refreshView() {
        if (mRootView == null || mArguments == null) {
            return;
        }
        Currency currency = Currency.EUR;
        Tax tax = Tax.ATI;
        Vendor vendor = Vendor.AMAZON;
        if (mArguments.containsKey(PrepareOrderActivity.EXTRA_CURRENCY)) {
            currency = mArguments.getParcelable(PrepareOrderActivity.EXTRA_CURRENCY);
        }

        if (mArguments.containsKey(PrepareOrderActivity.EXTRA_VENDOR)) {
            vendor = mArguments.getParcelable(PrepareOrderActivity.EXTRA_VENDOR);
        }

        if (mArguments.containsKey(PrepareOrderActivity.EXTRA_TAX)) {
            tax = mArguments.getParcelable(PrepareOrderActivity.EXTRA_TAX);
        }
        if (mArguments.containsKey(PrepareOrderActivity.EXTRA_SHIPPING_PRICE)) {
            mShippingFees.setText(getString(R.string.shopelia_product_shipping_fees,
                    currency.format(mArguments.getFloat(PrepareOrderActivity.EXTRA_SHIPPING_PRICE))));
            mShippingFees.setVisibility(View.VISIBLE);
        } else {
            mShippingFees.setVisibility(View.GONE);
        }

        mProductName.setText(mArguments.getString(PrepareOrderActivity.EXTRA_PRODUCT_TITLE));
        mProductDescription.setText(mArguments.getString(PrepareOrderActivity.EXTRA_PRODUCT_DESCRIPTION));
        mProductDescription.setVisibility(mArguments.containsKey(PrepareOrderActivity.EXTRA_PRODUCT_DESCRIPTION) ? View.VISIBLE : View.GONE);
        mProductPrice.setText(currency.format(mArguments.getFloat(PrepareOrderActivity.EXTRA_PRICE)));
        mProductShippingInfo.setText(mArguments.getString(PrepareOrderActivity.EXTRA_SHIPPING_INFO));
        mProductShippingInfo.setVisibility(mArguments.containsKey(PrepareOrderActivity.EXTRA_SHIPPING_INFO) ? View.VISIBLE : View.GONE);
        mTax.setText(tax.getResId());
        mVendorLogo.setImageResource(vendor.getImageResId());
        Object image = mArguments.get(PrepareOrderActivity.EXTRA_PRODUCT_IMAGE);
        if (image != null && image instanceof Uri) {
            mProductImage.setImageURI((Uri) image);
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
