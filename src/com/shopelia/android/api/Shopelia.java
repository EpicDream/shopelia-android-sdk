package com.shopelia.android.api;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment.SavedState;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.WelcomeActivity;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Merchant;
import com.shopelia.android.remote.api.ApiHandler;
import com.shopelia.android.remote.api.MerchantsAPI;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.Tax;

/**
 * This class eases the use of the Shopelia service and should be the only one
 * used by developers. It implements {@link Parcelable} so you can store it in
 * {@link Intent}, {@link SavedState} or {@link Bundle}
 * 
 * @author Pierre Pollastri
 */
public final class Shopelia implements Parcelable {

    public interface Callback {

        public void onShopeliaIsAvailable(Shopelia instance);

        public void onUpdateDone();

    }

    public static class CallbackAdapter implements Callback {

        @Override
        public void onShopeliaIsAvailable(Shopelia instance) {

        }

        @Override
        public void onUpdateDone() {

        }

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
     * Title of the product to purchase
     */
    public static final String EXTRA_PRODUCT_TITLE = PrepareOrderActivity.EXTRA_PRODUCT_TITLE;

    /**
     * Description of the product to purchase
     */
    public static final String EXTRA_PRODUCT_DESCRIPTION = PrepareOrderActivity.EXTRA_PRODUCT_DESCRIPTION;

    /**
     * The {@link Merchant} of the product to purchase
     */
    public static final String EXTRA_MERCHANT = PrepareOrderActivity.EXTRA_MERCHANT;

    /**
     * The price of the product to purchase
     */
    public static final String EXTRA_PRICE = PrepareOrderActivity.EXTRA_PRICE;

    /**
     * The shipping fees of the product to purchase
     */
    public static final String EXTRA_SHIPPING_PRICE = PrepareOrderActivity.EXTRA_SHIPPING_PRICE;

    /**
     * The shipping info of the product to purchase
     */
    public static final String EXTRA_SHIPPING_INFO = PrepareOrderActivity.EXTRA_SHIPPING_INFO;

    /**
     * A {@link Tax} object
     */
    public static final String EXTRA_TAX = PrepareOrderActivity.EXTRA_TAX;

    /**
     * The {@link Currency} of the price
     */
    public static final String EXTRA_CURRENCY = PrepareOrderActivity.EXTRA_CURRENCY;

    /**
     * An email that will be pre-filled for the user
     */
    public static final String EXTRA_USER_EMAIL = PrepareOrderActivity.EXTRA_USER_EMAIL;

    /**
     * An phone that will be pre-filled for the user
     */
    public static final String EXTRA_USER_PHONE = PrepareOrderActivity.EXTRA_USER_PHONE;

    public static final int RESULT_SUCCESS = Activity.RESULT_OK;
    public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
    public static final int RESULT_REDIRECT_ON_MERCHANT = 0x1602;

    public static final int REQUEST_SHOPELIA = ShopeliaActivity.REQUEST_CHECKOUT;

    private Intent mData;
    private Context mContext;

    private Shopelia(Context context, String productUrl, Merchant merchant) {
        mData = new Intent();
        mData.putExtra(EXTRA_PRODUCT_URL, productUrl);
        mData.putExtra(EXTRA_MERCHANT, merchant);
        mContext = context;
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
    public static final void checkout(Context context, String productUrl, Intent data, int requestCode) {
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
     * Obtains a new instance of {@link Shopelia} only if the merchant of the
     * product url is available on Shopelia. Even if the method returns null, it
     * will check online if the merchant is available and notify you later.
     * 
     * @param context
     * @param productUrl The product url
     * @param callback The {@link Callback} instance used to notify you that the
     *            merchant is available
     * @return
     */
    public static Shopelia obtain(final Context context, final String productUrl, final Callback callback) {
        MerchantsAPI api = new MerchantsAPI(context, new ApiHandler.CallbackAdapter() {
            @Override
            public void onRetrieveMerchant(Merchant merchant) {
                super.onRetrieveMerchant(merchant);
                if (callback != null) {
                    callback.onShopeliaIsAvailable(new Shopelia(context, productUrl, merchant));
                }
            }
        });
        Merchant out = api.getMerchant(productUrl);
        if (out != null) {
            return new Shopelia(context, productUrl, out);
        }
        return null;
    }

    /**
     * Obtains a new instance of {@link Shopelia} only if the merchant of the
     * product url is available on Shopelia.
     * 
     * @param context
     * @param productUrl
     * @return
     */
    public static Shopelia obtain(final Context context, final String productUrl) {
        return obtain(context, productUrl, null);
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
     * Sets the product name
     * 
     * @param productName
     * @return
     */
    public Shopelia setProductName(String productName) {
        mData.putExtra(EXTRA_PRODUCT_TITLE, productName);
        return this;
    }

    /**
     * Sets the product price (without shipping price
     * 
     * @param price
     * @return
     */
    public Shopelia setProductPrice(float price) {
        mData.putExtra(EXTRA_PRICE, price);
        return this;
    }

    /**
     * Sets the product delivery price
     * 
     * @param price
     * @return
     */
    public Shopelia setProductShippingPrice(float price) {
        mData.putExtra(EXTRA_SHIPPING_PRICE, price);
        return this;
    }

    /**
     * Sets the extras informations associated with the product (stock, delivery
     * delay...)
     * 
     * @param info
     * @return
     */
    public Shopelia setProductShippingInfo(String info) {
        mData.putExtra(EXTRA_SHIPPING_INFO, info);
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

    /**
     * Update Shopelia's data. This method is useful if you want to avoid
     * retrieving {@link Shopelia} instances asynchronously. Once update is done
     * it will call {@link Callback#onUpdateDone()}. You should do your Shopelia
     * operations in this method.
     */
    public static void update(Context context, final Callback callback) {
        new MerchantsAPI(context, new com.shopelia.android.remote.api.ApiHandler.CallbackAdapter() {
            @Override
            public void onRetrieveMerchants(ArrayList<Merchant> merchants) {
                super.onRetrieveMerchants(merchants);
                if (callback != null) {
                    callback.onUpdateDone();
                }
            }
        }).update();
    }

}
