package com.shopelia.android.api.widget;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.ObjectAnimator;
import com.shopelia.android.R;
import com.shopelia.android.api.Shopelia.OnProductAvailabilityChangeListener;

public class ShopeliaFrameLayout extends FrameLayout implements ShopeliaView, ShopeliaViewHelper.Callback {

    private ShopeliaViewHelper mHelper;
    private View mDelegate;

    public ShopeliaFrameLayout(Context context) {
        this(context, null);
    }

    public ShopeliaFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShopeliaFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHelper = new ShopeliaViewHelper(context, attrs, isInEditMode());
        mHelper.setCallback(this);
        setCheckoutDelegate(this);
        onViewShouldBeInvisible();
    }

    @Override
    public void callCheckout() {
        mHelper.callCheckout();
    }

    @Override
    public void setProductUrl(CharSequence url) {
        mHelper.setProductUrl(url);
    }

    @Override
    public String getProductUrl() {
        return mHelper.getProductUrl();
    }

    @Override
    public boolean canCheckout() {
        return mHelper.canCheckout();
    }

    @Override
    public void setProductPrice(float price) {
        mHelper.setProductPrice(price);
    }

    @Override
    public void setProductDeliveryPrice(float shippingPrice) {
        mHelper.setProductDeliveryPrice(shippingPrice);
    }

    @Override
    public void setProductImage(Uri imageUri) {
        mHelper.setProductImage(imageUri);
    }

    @Override
    public void setProductShippingExtras(String shippingExtras) {
        mHelper.setProductShippingExtras(shippingExtras);
    }

    @Override
    public void onViewShouldBeInvisible() {
        setVisibility(View.GONE);
    }

    @Override
    public void onViewShouldSmoothlyAppear() {
        setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(this, "alpha", 0.f, 1.f).setDuration(getResources().getInteger(R.integer.shopelia_animation_time)).start();
    }

    @Override
    public void onViewShouldSmoothlyDisappear() {

    }

    @Override
    public void onViewShouldBeVisible() {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void onCheckout() {

    }

    public void setCheckoutDelegate(View v) {
        v.setOnClickListener(mOnCheckoutClickListener);
        if (mDelegate != null) {
            mDelegate.setOnClickListener(null);
        }
        mDelegate = v;
    }

    private OnClickListener mOnCheckoutClickListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            callCheckout();
        }
    };

    @Override
    public void setOnProductAvailabilityChangeListener(OnProductAvailabilityChangeListener l) {
        mHelper.setOnProductAvailabilityChangeListener(l);
    }

    @Override
    public void setProductName(String name) {
        mHelper.setProductName(name);
    }

    @Override
    public void setTrackerName(String name) {
        mHelper.setTrackerName(name);
    }

}
