package com.shopelia.android.api;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.Fragment.SavedState;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.WelcomeActivity;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.app.tracking.Tracker;
import com.shopelia.android.config.Config;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Merchant;

/**
 * This class eases the use of the Shopelia service and should be the only one
 * used by developers. It implements {@link Parcelable} so you can store it in
 * {@link Intent}, {@link SavedState} or {@link Bundle}
 * 
 * @author Pierre Pollastri
 */
public final class Shopelia implements Parcelable {

    /**
     * Listener class for observing status changes of an instance of Shopelia.
     * 
     * @author Pierre Pollastri
     */
    public interface OnProductAvailabilityChangeListener {
        /**
         * Called when the status of the instance state changes.
         * 
         * @param shopelia The changed instance.
         * @param newStatus The new status may be one of the Shopelia.STATUS_*
         *            integers.
         */
        public void onProductAvailabilityChanged(Shopelia shopelia, int newStatus);
    }

    /**
     * Url of the product to purchase
     */
    public static final String EXTRA_PRODUCT_URL = PrepareOrderActivity.EXTRA_PRODUCT_URL;

    /**
     * A resource ID or {@link Uri} representing the image of product to
     * purchase
     */
    public static final String EXTRA_PRODUCT_IMAGE = PrepareOrderActivity.EXTRA_PRODUCT_IMAGE;

    /**
     * An email that will be pre-filled for the user
     */
    public static final String EXTRA_USER_EMAIL = PrepareOrderActivity.EXTRA_USER_EMAIL;

    /**
     * An phone that will be pre-filled for the user
     */
    public static final String EXTRA_USER_PHONE = PrepareOrderActivity.EXTRA_USER_PHONE;

    /**
     * A boolean stating if the display screen must display
     */
    public static final String EXTRA_DISPLAY_WELCOME_SCREEN = Config.EXTRA_PREFIX + "DISPLAY_WELCOME_SCREEN";

    public static final int RESULT_SUCCESS = Activity.RESULT_OK;
    public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
    public static final int RESULT_REDIRECT_ON_MERCHANT = 0x1602;

    public static final int REQUEST_SHOPELIA = ShopeliaActivity.REQUEST_CHECKOUT;

    public static final int STATUS_SEARCHING = -1;
    public static final int STATUS_NOT_AVAILABLE = 0;
    public static final int STATUS_AVAILABLE = 1;
    public static final int STATUS_INVALID_URL = -2;

    private Intent mData;
    private int mStatus = STATUS_SEARCHING;
    private OnProductAvailabilityChangeListener mOnProductAvailabilityChangeListener;
    private ShopeliaController mController;
    private boolean mHasNotifyView = false;
    private Tracker mTracker;
    private String mTrackerName;

    public Shopelia(Context context, String productUrl, String trackerName, OnProductAvailabilityChangeListener l) {
        setOnProductAvailabilityChangeListener(l);
        mTrackerName = trackerName;
        mTracker = Tracker.Factory.getTracker(Tracker.PROVIDER_SHOPELIA, context);
        mData = new Intent();
        mData.putExtra(EXTRA_PRODUCT_URL, productUrl);
        mController = ShopeliaController.getInstance();
        try {
            new URL(productUrl);
        } catch (MalformedURLException e) {
            productUrl = null;
        }
        if (TextUtils.isEmpty(productUrl)) {
            setStatus(STATUS_INVALID_URL);
        } else {
            mController.fetch(context.getApplicationContext(), this);
        }
    }

    public void setOnProductAvailabilityChangeListener(OnProductAvailabilityChangeListener l) {
        mOnProductAvailabilityChangeListener = l;
    }

    /**
     * Notify to Shopelia that the product is currently visible on the
     * application User Interface.
     */
    public void notifyView() {
        if (!mHasNotifyView) {
            mTracker.onDisplayShopeliaButton(getProductUrl(), mTrackerName);
            mHasNotifyView = true;
        }
    }

    void setStatus(final int status) {
        if (status != mStatus) {
            mStatus = status;
            if (mOnProductAvailabilityChangeListener != null) {
                mOnProductAvailabilityChangeListener.onProductAvailabilityChanged(this, status);
            }
        }
    }

    /**
     * Gets the url of the product for this instance of Shopelia.
     * 
     * @return The url of the product
     */
    public String getProductUrl() {
        return mData.getStringExtra(EXTRA_PRODUCT_URL);
    }

    /**
     * Get the current status of this instance of shopelia.
     * 
     * @return {@link Shopelia#STATUS_AVAILABLE} if the product is available,
     *         {@link Shopelia#STATUS_SEARCHING} if Shopelia is looking for the
     *         availability of the product.
     *         {@link Shopelia#STATUS_NOT_AVAILABLE} if the product is not
     *         available on Shopelia.
     */
    public int getStatus() {
        return mStatus;
    }

    private Shopelia(Parcel source) {
        mData = source.readParcelable(Intent.class.getClassLoader());
    }

    /**
     * Checkouts the product of the given url
     * 
     * @param productUrl The product URL
     * @param data Extra data or null if unnecessary
     */
    private final void checkout(Context context, String productUrl, Intent data, int requestCode) {
        notifyView();
        if (data == null) {
            data = new Intent();
        }
        data.setExtrasClassLoader(Merchant.class.getClassLoader());
        if (UserManager.get(context).getCheckoutCount() == 0) {
            data.setClass(context, WelcomeActivity.class);
        } else {
            data.setClass(context, WelcomeActivity.class);
        }
        if (productUrl != null) {
            data.putExtra(EXTRA_PRODUCT_URL, productUrl);
        }
        data.putExtra(ShopeliaActivity.EXTRA_STYLE, ShopeliaActivity.STYLE_DIALOG);
        mTracker.onClickShopeliaButton(getProductUrl(), mTrackerName);
        mTracker.flush();
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(data, requestCode);
        } else {
            context.startActivity(data);
        }
    }

    public void checkout(Context context) {
        checkout(context, null, mData, REQUEST_SHOPELIA);
    }

    public void checkout(Context context, int requestCode) {
        checkout(context, null, mData, requestCode);
    }

    /**
     * Sets the image uri of the product
     * 
     * @param imageUri
     * @return
     */
    public Shopelia setProductImageUri(Uri imageUri) {
        mData.putExtra(EXTRA_PRODUCT_IMAGE, imageUri);
        return this;
    }

    /**
     * Sets a possible email for the user. It can ease the registration of the
     * user on Shopelia if you already know it.
     * 
     * @param email
     * @return
     */
    public Shopelia setUserEmail(String email) {
        mData.putExtra(EXTRA_USER_EMAIL, email);
        return this;
    }

    /**
     * Sets if the welcome screen should be displayed or not. By default a
     * Welcome screen is displayed. This screen explains to the user what is
     * Shopelia and how does it work.
     * 
     * @param display True for displaying the screen (default behaviour)
     */
    public Shopelia setDisplayWelcomeScreen(boolean display) {
        mData.putExtra(EXTRA_DISPLAY_WELCOME_SCREEN, display);
        return this;
    }

    /**
     * Sets a possible phone for the user. It can ease the registration of the
     * user on Shopelia if you already know it.
     * 
     * @param phone
     * @return
     */
    public Shopelia setUserPhone(String phone) {
        mData.putExtra(EXTRA_USER_PHONE, phone);
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mData, flags);
    }

    public static final Parcelable.Creator<Shopelia> CREATOR = new Creator<Shopelia>() {

        @Override
        public Shopelia[] newArray(int size) {
            return new Shopelia[size];
        }

        @Override
        public Shopelia createFromParcel(Parcel source) {
            return new Shopelia(source);
        }
    };

}
