package com.shopelia.android.api.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

public class ShopeliaButton extends Button implements ShopeliaView {

    private ShopeliaViewHelper mHelper;

    public ShopeliaButton(Context context) {
        this(context, null);
    }

    public ShopeliaButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShopeliaButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHelper = new ShopeliaViewHelper(context, attrs);
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
        Log.d(VIEW_LOG_TAG, "TEST DE BUTTON");
        return super.performClick();
    }

    @Override
    public String getProductUrl() {
        return mHelper.getProductUrl();
    }

}
