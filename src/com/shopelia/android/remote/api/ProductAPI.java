package com.shopelia.android.remote.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.http.AbstractPoller.OnPollerEventListener;
import com.shopelia.android.http.HttpGetPoller;
import com.shopelia.android.http.HttpGetPoller.HttpGetRequest;
import com.shopelia.android.http.HttpGetPoller.HttpGetResponse;
import com.shopelia.android.model.Product;
import com.shopelia.android.utils.TimeUnits;
import com.turbomanage.httpclient.ParameterMap;

public class ProductAPI extends ApiHandler {

    private static final long POLLING_FREQUENCY = TimeUnits.SECONDS / 2;
    private static final long POLLING_EXPIRATION = 10 * TimeUnits.SECONDS;

    private HttpGetPoller mPoller;
    private Product mProduct;

    public ProductAPI(Context context, Callback callback) {
        super(context, callback);
    }

    public boolean getProduct(Product product) {
        mProduct = product;
        if (!product.isValid()) {
            if (mPoller != null) {
                mPoller.stop();
            }
            ShopeliaRestClient client = ShopeliaRestClient.V1(getContext());
            ParameterMap map = client.newParams();
            map.add(Product.Api.URL, mProduct.url);
            mPoller = new HttpGetPoller(client);
            mPoller.setExpiryDuration(POLLING_EXPIRATION).setRequestFrequency(POLLING_FREQUENCY)
                    .setParam(new HttpGetRequest(Command.V1.Products.$, map)).setOnPollerEventListener(mOnPollerEventListener).poll();
            return false;
        }
        return true;
    }

    public Product getProduct() {
        return mProduct;
    }

    private OnPollerEventListener<HttpGetResponse> mOnPollerEventListener = new OnPollerEventListener<HttpGetPoller.HttpGetResponse>() {

        @Override
        public void onTimeExpired() {
            if (hasCallback()) {
                getCallback().onProductNotAvailable(mProduct);
            }
        }

        @Override
        public boolean onResult(HttpGetResponse previousResult, HttpGetResponse newResult) {
            if (newResult.exception != null) {
                if (hasCallback()) {
                    getCallback().onError(STEP_PRODUCT, newResult.response, null, newResult.exception);
                }
            } else {
                try {
                    mProduct = Product.inflate(new JSONObject(newResult.response.getBodyAsString()));
                    if (hasCallback()) {
                        getCallback().onProductUpdate(mProduct);
                    }
                    return mProduct.isValid();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

    };

}
