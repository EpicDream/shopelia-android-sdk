package com.shopelia.android.api.widget;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import com.shopelia.android.api.Shopelia;
import com.shopelia.android.api.Shopelia.OnProductAvailabilityChangeListener;
import com.shopelia.android.utils.TimeUnits;

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

    private static final long DELAY_FOR_SMOOTH_CHANGES = 10 * TimeUnits.MILISECONDS;

    private Context mContext;
    private String mProductUrl;
    private Shopelia mShopelia;
    private Callback mCallback;
    private String mTrackerName;
    private OnProductAvailabilityChangeListener mOnProductAvailabilityChangeListener;

    public ShopeliaViewHelper(Context context, AttributeSet attrs, boolean editMode) {
        mContext = context;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
        if (mProductUrl == null) {
            mCallback.onViewShouldBeInvisible();
        }
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
            final long begin = System.currentTimeMillis();
            mShopelia = new Shopelia(mContext, mProductUrl, mTrackerName, new OnProductAvailabilityChangeListener() {

                @Override
                public void onProductAvailabilityChanged(Shopelia shopelia, int newStatus) {
                    if (mCallback != null) {
                        switch (newStatus) {
                            case Shopelia.STATUS_AVAILABLE:
                                if (begin + DELAY_FOR_SMOOTH_CHANGES < System.currentTimeMillis()) {
                                    mCallback.onViewShouldSmoothlyAppear();
                                } else {
                                    mCallback.onViewShouldBeVisible();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    if (mOnProductAvailabilityChangeListener != null) {
                        mOnProductAvailabilityChangeListener.onProductAvailabilityChanged(shopelia, newStatus);
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
        return mShopelia != null;
    }

    @Override
    public void setProductPrice(float price) {
        if (mShopelia != null) {
            mShopelia.setProductPrice(price);
        }
    }

    @Override
    public void setProductDeliveryPrice(float shippingPrice) {
        if (mShopelia != null) {
            mShopelia.setProductShippingPrice(shippingPrice);
        }
    }

    @Override
    public void setProductImage(Uri imageUri) {
        if (mShopelia != null) {
            mShopelia.setProductImageUri(imageUri);
        }
    }

    @Override
    public void setProductShippingExtras(String shippingExtras) {
        if (mShopelia != null) {
            mShopelia.setProductShippingInfo(shippingExtras);
        }
    }

    @Override
    public void setOnProductAvailabilityChangeListener(OnProductAvailabilityChangeListener l) {
        mOnProductAvailabilityChangeListener = l;
    }

    @Override
    public void setProductName(String name) {
        if (mShopelia != null) {
            mShopelia.setProductName(name);
        }
    }

    @Override
    public void setTrackerName(String name) {
        mTrackerName = name;
    }

}
