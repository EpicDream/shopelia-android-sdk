package com.shopelia.android.api.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;

import com.nineoldandroids.animation.ObjectAnimator;
import com.shopelia.android.R;
import com.shopelia.android.api.Shopelia.OnProductAvailabilityChangeListener;
import com.shopelia.android.widget.ValidationButton;

public class ShopeliaButton extends ValidationButton implements ShopeliaView, ShopeliaViewHelper.Callback {

    private ShopeliaViewHelper mHelper;

    public ShopeliaButton(Context context) {
        this(context, null);
    }

    public ShopeliaButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("NewApi")
    public ShopeliaButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, 0);
        mHelper = new ShopeliaViewHelper(context, attrs, isInEditMode());
        mHelper.setCallback(this);
        int[] attrsArray = new int[] {
            android.R.attr.text
        };
        TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);
        String text = ta.getString(0);
        ta.recycle();
        setText(text != null ? text : context.getString(R.string.shopelia_common_buy));
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
    public boolean performClick() {
        callCheckout();
        return canCheckout() && super.performClick();
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
    public void onViewShouldBeInvisible() {
        setVisibility(View.INVISIBLE);
    }

    @Override
    public void onViewShouldSmoothlyAppear() {
        onViewShouldBeVisible();
        startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
        ObjectAnimator a = ObjectAnimator.ofFloat(this, "scaleX", 0.f, 1.f);
        a.setInterpolator(new BounceInterpolator());
        a.setDuration(1000).start();
        a = ObjectAnimator.ofFloat(this, "scaleY", 0.f, 1.f);
        a.setInterpolator(new BounceInterpolator());
        a.setDuration(1000).start();

    }

    @Override
    public void onViewShouldSmoothlyDisappear() {
        setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        anim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onViewShouldBeVisible() {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void onCheckout() {

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
