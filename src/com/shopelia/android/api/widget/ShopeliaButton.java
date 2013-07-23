package com.shopelia.android.api.widget;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.shopelia.android.widget.ValidationButton;

public class ShopeliaButton extends ValidationButton implements ShopeliaView, ShopeliaViewHelper.Callback {

    private ShopeliaViewHelper mHelper;

    public ShopeliaButton(Context context) {
        this(context, null);
    }

    public ShopeliaButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShopeliaButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, 0);
        mHelper = new ShopeliaViewHelper(context, attrs);
        mHelper.setCallback(this);
        setText("Acheter");
    }

    @Override
    public void callCheckout() {
        mHelper.callCheckout();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {

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
        // TODO Auto-generated method stub

    }

    @Override
    public void setProductDeliveryPrice(float shippingPrice) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setProductImage(Uri imageUri) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setProductShippingExtras(String shippingExtras) {
        // TODO Auto-generated method stub

    }

}
