package com.shopelia.android.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.shopelia.android.R;
import com.shopelia.android.model.Product;

public class ProductSheetWidget extends FrameLayout {

    private View mRootView;

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

    private Product mProduct;

    public ProductSheetWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (mRootView == null) {
            mRootView = onCreateView(LayoutInflater.from(context));
            onViewCreated();
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

    public ProductSheetWidget setProductInfo(Product product) {
        return this;
    }

    public void refreshView() {

    }

    public String getString(int id) {
        return mRootView.getContext().getString(id);
    }

    public String getString(int id, Object... args) {
        return mRootView.getContext().getString(id, args);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState ss = new SavedState(parcelable);
        ss.product = mProduct;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mProduct = ss.product;
    }

    private static class SavedState extends View.BaseSavedState {

        public Product product;

        public SavedState(Parcel parcel) {
            super(parcel);
            product = parcel.readParcelable(Product.class.getClassLoader());
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(product, flags);
        }

        public static final Creator<SavedState> CREATOR = new Creator<ProductSheetWidget.SavedState>() {

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }
        };

    }

}
