package com.shopelia.android.remote.api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;

import com.shopelia.android.http.AbstractPoller.OnPollerEventListener;
import com.shopelia.android.http.HttpGetPoller;
import com.shopelia.android.http.HttpGetPoller.HttpGetRequest;
import com.shopelia.android.http.HttpGetPoller.HttpGetResponse;
import com.shopelia.android.model.ExtendedProduct;
import com.shopelia.android.model.Product;
import com.shopelia.android.utils.JsonUtils;
import com.shopelia.android.utils.TimeUnits;
import com.turbomanage.httpclient.ParameterMap;

/**
 * Reliable for only one product at the same (use multiple instances for
 * multiple products)
 * 
 * @author Pierre Pollastri
 */
public class ProductAPI extends ApiHandler {

    private static final String PRIVATE_PREFERENCE = "Shopelia$ProductAPI.PrivatePreference";
    private static final String PREFS_PRODUCT = "product:products";

    private static final long KEEP_ALIVE = 20 * TimeUnits.MINUTES;

    private static final long POLLING_FREQUENCY = TimeUnits.SECONDS / 2;
    private static final long POLLING_EXPIRATION = 10 * TimeUnits.SECONDS;

    private HttpGetPoller mPoller;
    private SharedPreferences mPreferences;
    private ArrayList<ExtendedProduct> mProducts;
    private ExtendedProduct mProduct;
    private Handler mHandler = new Handler();

    public ProductAPI(Context context, Callback callback) {
        super(context, callback);
        mPreferences = context.getSharedPreferences(PRIVATE_PREFERENCE, Context.MODE_PRIVATE);
        loadProductsFromCache();
    }

    public boolean getProduct(Product product) {
        mProduct = new ExtendedProduct(product);
        ExtendedProduct fromCache = findProductByUrl(product.url);
        if (fromCache != null && !product.isValid()) {
            mProduct = fromCache;
        }
        if (mProduct.getProduct() == null || !mProduct.getProduct().isValid()) {
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
        } else {
            if (hasCallback()) {
                getCallback().onProductUpdate(mProduct.getProduct(), false);
            }
        }
        return true;
    }

    public Product getProduct() {
        return mProduct.getProduct();
    }

    private OnPollerEventListener<HttpGetResponse> mOnPollerEventListener = new OnPollerEventListener<HttpGetPoller.HttpGetResponse>() {

        @Override
        public void onTimeExpired() {
            if (hasCallback()) {
                getCallback().onProductNotAvailable(mProduct.getProduct());
            }
        }

        @Override
        public boolean onResult(HttpGetResponse previousResult, final HttpGetResponse newResult) {
            if (newResult.exception != null) {
                if (hasCallback()) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            getCallback().onError(STEP_PRODUCT, newResult.response, null, newResult.exception);
                        }
                    });
                }
            } else if (newResult.response != null) {
                try {
                    mProduct.setJson(new JSONObject(newResult.response.getBodyAsString()));
                    mProduct.download_at = System.currentTimeMillis();
                    mProducts.add(mProduct);
                    saveProducts(mProducts);
                    if (hasCallback() && mProduct.isValid() && mProduct.ready) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                getCallback().onProductUpdate(mProduct.getProduct(), true);
                            }
                        });
                    }
                    return mProduct.isValid();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

    };

    private ExtendedProduct findProductByUrl(String url) {
        for (ExtendedProduct product : mProducts) {
            if (product.url.equals(url)) {
                if (product.download_at + KEEP_ALIVE < System.currentTimeMillis() || !product.isValid()) {
                    mProducts.remove(product);
                    saveProducts(mProducts);
                    return null;
                }
                return product;
            }
        }
        return null;
    }

    private void loadProductsFromCache() {
        String json = mPreferences.getString(PREFS_PRODUCT, null);
        if (!TextUtils.isEmpty(json)) {
            try {
                mProducts = ExtendedProduct.inflate(new JSONArray(json));
            } catch (Exception e) {
                mProducts = new ArrayList<ExtendedProduct>();
            }
        } else {
            mProducts = new ArrayList<ExtendedProduct>();
        }
    }

    private void saveProducts(List<ExtendedProduct> products) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREFS_PRODUCT, JsonUtils.toJson(products).toString());
        editor.commit();
    }

}
