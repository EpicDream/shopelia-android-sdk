package com.shopelia.android.remote.api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.shopelia.android.http.AbstractPoller.OnPollerEventListener;
import com.shopelia.android.http.HttpGetPoller;
import com.shopelia.android.http.HttpGetPoller.HttpGetRequest;
import com.shopelia.android.http.HttpGetPoller.HttpGetResponse;
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

    private static final long KEEP_ALIVE = 1 * TimeUnits.HOURS;

    private static final long POLLING_FREQUENCY = TimeUnits.SECONDS / 2;
    private static final long POLLING_EXPIRATION = 10 * TimeUnits.SECONDS;

    private HttpGetPoller mPoller;
    private SharedPreferences mPreferences;
    private ArrayList<Product> mProducts;
    private Product mProduct;

    public ProductAPI(Context context, Callback callback) {
        super(context, callback);
        mPreferences = context.getSharedPreferences(PRIVATE_PREFERENCE, Context.MODE_PRIVATE);
        loadProductsFromCache();
    }

    public boolean getProduct(Product product) {
        mProduct = product;
        Product fromCache = findProductByUrl(product.url);
        if (fromCache != null) {
            mProduct = fromCache;
        }
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
        } else {
            if (hasCallback()) {
                getCallback().onProductUpdate(mProduct);
            }
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
                    mProduct.download_at = System.currentTimeMillis();
                    mProducts.add(mProduct);
                    saveMerchants(mProducts);
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

    private Product findProductByUrl(String url) {
        for (Product product : mProducts) {
            if (product.url.equals(url)) {
                if (product.download_at + KEEP_ALIVE > System.currentTimeMillis()) {
                    mProducts.remove(product);
                    saveMerchants(mProducts);
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
                mProducts = Product.inflate(new JSONArray(json));
            } catch (Exception e) {
                mProducts = new ArrayList<Product>();
            }
        } else {
            mProducts = new ArrayList<Product>();
        }
    }

    private void saveMerchants(List<Product> products) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREFS_PRODUCT, JsonUtils.toJson(products).toString());
        editor.commit();
    }

}
