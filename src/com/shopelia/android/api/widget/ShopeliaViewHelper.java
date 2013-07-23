package com.shopelia.android.api.widget;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import com.shopelia.android.api.Shopelia;

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

    public interface Callback {
        public void onViewShouldBeInvisible();

        public void onViewShouldSmoothlyAppear();

        public void onViewShouldSmoothlyDisappear();

        public void onViewShouldBeVisible();

        public void onCheckout();
    }

    private Context mContext;
    private String mProductUrl;
    private Shopelia mShopelia;
    private Callback mCallback;

    public ShopeliaViewHelper(Context context, AttributeSet attrs) {
        mContext = context;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void callCheckout() {
        if (mShopelia != null) {
            mShopelia.checkout(mContext);
        }
    }

    @Override
    public void setProductUrl(CharSequence url) {
        if (url != null && !url.toString().equals(mProductUrl)) {
            mProductUrl = url.toString();

            Shopelia.update(mContext, new Shopelia.CallbackAdapter() {
                @Override
                public void onUpdateDone() {
                    super.onUpdateDone();
                    mShopelia = Shopelia.obtain(mContext, mProductUrl);
                    if (mShopelia != null && mCallback != null) {
                        mCallback.onViewShouldSmoothlyAppear();
                    }
                }
            });
        }
    }

    @Override
    public String getProductUrl() {
        return mProductUrl;
    }

    @Override
    public boolean canCheckout() {
        return false;
    }

    @Override
    public void setProductPrice(float price) {

    }

    @Override
    public void setProductDeliveryPrice(float shippingPrice) {

    }

    @Override
    public void setProductImage(Uri imageUri) {

    }

    @Override
    public void setProductShippingExtras(String shippingExtras) {

    }

}