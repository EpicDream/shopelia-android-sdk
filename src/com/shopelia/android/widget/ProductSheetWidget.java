package com.shopelia.android.widget;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.shopelia.android.R;
import com.shopelia.android.http.AbstractPoller.OnPollerEventListener;
import com.shopelia.android.http.HttpGetPoller;
import com.shopelia.android.http.HttpGetPoller.HttpGetRequest;
import com.shopelia.android.http.HttpGetPoller.HttpGetResponse;
import com.shopelia.android.model.Product;
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.shopelia.android.utils.TimeUnits;
import com.turbomanage.httpclient.ParameterMap;

public class ProductSheetWidget extends FrameLayout {

    private static final long POLLING_FREQUENCY = TimeUnits.SECONDS / 2;
    private static final long POLLING_EXPIRATION = 10 * TimeUnits.SECONDS;

    private View mRootView;

    // Views
    private FontableTextView mProductName;
    private FontableTextView mProductDescription;
    private FontableTextView mProductShippingInfo;
    private FontableTextView mShippingFees;
    private AsyncImageView mProductImage;
    @SuppressWarnings("unused")
    private FontableTextView mVendorText;
    private AsyncImageView mVendorLogo;
    private FontableTextView mProductPrice;
    private FontableTextView mTax;

    private Product mProduct;
    private HttpGetPoller mPoller;

    public ProductSheetWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (mRootView == null) {
            mRootView = onCreateView(LayoutInflater.from(context));
            onViewCreated();
        }
    }

    public View onCreateView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.shopelia_product_sheet, this, true);
    }

    public void onViewCreated() {
        mProductName = (FontableTextView) findViewById(R.id.product_name);
        mProductDescription = (FontableTextView) findViewById(R.id.product_description);
        mProductShippingInfo = (FontableTextView) findViewById(R.id.product_shipping_info);
        mShippingFees = (FontableTextView) findViewById(R.id.product_shipping_fees);
        mProductImage = (AsyncImageView) findViewById(R.id.product_image);
        mProductPrice = (FontableTextView) findViewById(R.id.product_price);
        mVendorLogo = (AsyncImageView) findViewById(R.id.product_vendor_icon);
        mVendorText = (FontableTextView) findViewById(R.id.product_vendor_text);
        mTax = (FontableTextView) findViewById(R.id.product_tax);
    }

    public ProductSheetWidget setProductInfo(Product product) {
        mProduct = product;
        if (product.productPrice == Product.NO_PRICE) {
            if (mPoller != null) {
                mPoller.stop();
            }
            ShopeliaRestClient client = ShopeliaRestClient.V1(getContext());
            ParameterMap map = client.newParams();
            map.add(Product.Api.URL, mProduct.url);
            mPoller = new HttpGetPoller(client);
            mPoller.setExpiryDuration(POLLING_EXPIRATION).setRequestFrequency(POLLING_FREQUENCY)
                    .setParam(new HttpGetRequest(Command.V1.Products.$, map)).setOnPollerEventListener(mOnPollerEventListener).poll();
        }
        return this;
    }

    public void refreshView() {
        if (mProduct == null) {

        } else {
            mProductImage.setImageURI(mProduct.image);
            mProductName.setText(mProduct.name);
            mProductPrice.setText(mProduct.currency.format(mProduct.productPrice));
            mProductShippingInfo.setText(mProduct.shippingExtra);
            mShippingFees.setText(mProduct.currency.format(mProduct.deliveryPrice));
            mTax.setText(getString(mProduct.tax.getResId()));
            mVendorLogo.setUrl(mProduct.merchant.logo);
            mProductDescription.setVisibility(View.GONE);
        }
    }

    public String getString(int id) {
        return mRootView.getContext().getString(id);
    }

    public String getString(int id, Object... args) {
        return mRootView.getContext().getString(id, args);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState ss = new SavedState(parcelable);
        ss.product = mProduct;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mProduct = ss.product;
    }

    private static class SavedState extends View.BaseSavedState {

        public Product product;

        public SavedState(Parcel parcel) {
            super(parcel);
            product = parcel.readParcelable(Product.class.getClassLoader());
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(product, flags);
        }

        public static final Creator<SavedState> CREATOR = new Creator<ProductSheetWidget.SavedState>() {

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }
        };

    }

    private OnPollerEventListener<HttpGetResponse> mOnPollerEventListener = new OnPollerEventListener<HttpGetPoller.HttpGetResponse>() {

        @Override
        public void onTimeExpired() {

        }

        @Override
        public void onResult(HttpGetResponse previousResult, HttpGetResponse newResult) {
            if (newResult.exception != null) {
                newResult.exception.printStackTrace();
            } else {
                try {
                    mProduct = Product.inflate(new JSONObject(newResult.response.getBodyAsString()));
                    refreshView();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    };

}
