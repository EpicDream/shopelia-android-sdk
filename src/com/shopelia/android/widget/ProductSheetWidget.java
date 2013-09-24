package com.shopelia.android.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;

import com.shopelia.android.R;
import com.shopelia.android.model.Product;
import com.shopelia.android.view.animation.CrossFadingTransition;
import com.shopelia.android.view.animation.ResizeAnimation;
import com.shopelia.android.view.animation.ResizeAnimation.OnViewRectComputedListener;

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
    private View mLoading;
    private View mContent;
    private View mSwitcher;

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
        mVendorLogo.setDrawableAlignement(AsyncImageView.ALIGN_LEFT | AsyncImageView.ALIGN_CENTER_VERTICAL);
        mVendorText = (FontableTextView) findViewById(R.id.product_vendor_text);
        mTax = (FontableTextView) findViewById(R.id.product_tax);
        mLoading = findViewById(R.id.loading);
        mContent = findViewById(R.id.content);
        mSwitcher = findViewById(R.id.switcher);
    }

    public ProductSheetWidget setProductInfo(Product product) {
        return setProductInfo(product, true);
    }

    public ProductSheetWidget setProductInfo(Product product, boolean animate) {
        if (mProduct != null && !mProduct.isValid()) {
            mProduct = product;
            refreshView(animate);
        }
        return this;
    }

    public void refreshView() {
        refreshView(false);
    }

    public void refreshView(boolean animate) {
        if (mProduct == null || !mProduct.isValid()) {
            mLoading.setVisibility(View.VISIBLE);
            mContent.setVisibility(View.INVISIBLE);
        } else {

            mProductImage.setImageURI(mProduct.image);
            mProductName.setText(mProduct.name);
            mProductPrice.setText(mProduct.currency.format(mProduct.productPrice));
            mProductShippingInfo.setText(mProduct.shippingExtra);
            int visibility = TextUtils.isEmpty(mProduct.shippingExtra) ? View.GONE : View.VISIBLE;
            mProductShippingInfo.setVisibility(visibility);
            mTax.setText(getString(mProduct.tax.getResId()));
            mVendorLogo.setUrl(mProduct.merchant.logo);
            mProductDescription.setVisibility(View.GONE);
            if (mProduct.deliveryPrice == 0.0f) {
                mShippingFees.setText(R.string.shopelia_product_free_shipping);
            } else {
                mShippingFees.setText(getString(R.string.shopelia_product_shipping_fees, mProduct.currency.format(mProduct.deliveryPrice)));
            }
            if (animate) {
                switchViews();
            } else {
                mContent.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void switchViews() {
        final ResizeAnimation anim = new ResizeAnimation(mSwitcher, mSwitcher.getLayoutParams().width, mSwitcher.getLayoutParams().height);
        anim.setDuration(getResources().getInteger(R.integer.shopelia_animation_time_short));
        anim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                new CrossFadingTransition(mContent, mLoading, true).setDuration(
                        getResources().getInteger(R.integer.shopelia_animation_time)).start();
            }
        });
        anim.computeSize(new OnViewRectComputedListener() {

            @Override
            public void onViewRectComputed(View victim, Rect from, Rect to) {
                mSwitcher.startAnimation(anim);
            }
        });
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
