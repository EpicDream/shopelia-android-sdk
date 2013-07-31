package com.shopelia.android.api.widget;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

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
        mHelper = new ShopeliaViewHelper(context, attrs);
        setCheckoutDelegate(this);
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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHelper.onDetachFromWindow();
    }

    @Override
    public void onViewShouldBeInvisible() {
        setVisibility(View.GONE);
    }

    @Override
    public void onViewShouldSmoothlyAppear() {
        setVisibility(View.INVISIBLE);
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
        mDelegate = v;
        v.setOnClickListener(mOnCheckoutClickListener);
    }

    private OnClickListener mOnCheckoutClickListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            callCheckout();
        }
    };

}
