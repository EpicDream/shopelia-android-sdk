package com.shopelia.android.api.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * A helper class useful if you want to create a custom Shopelia layout. It
 * provides every implementation of a standard ShopeliaView. If you want to
 * create your owns {@link ShopeliaView}, you should create a {@link View}
 * implementing {@link ShopeliaView} and calling every method of this helper
 * class at the right place.
 * 
 * @author Pierre Pollastri
 */
public class ShopeliaViewHelper implements ShopeliaView {

    private Context mContext;
    private String mProductUrl;

    public ShopeliaViewHelper(Context context, AttributeSet attrs) {
        mContext = context;
    }

    @Override
    public void callCheckout() {

    }

    @Override
    public void setProductUrl(CharSequence url) {
        if (url != null && !url.toString().equals(mProductUrl)) {
            mProductUrl = url.toString();
        }
    }

    @Override
    public String getProductUrl() {
        return mProductUrl;
    }

}
