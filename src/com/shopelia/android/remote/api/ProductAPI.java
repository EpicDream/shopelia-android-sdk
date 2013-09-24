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
public class ProductAPI extends ApiController {

    public class OnProductUpdateEvent extends OnResourceEvent<Product> {

        public final boolean isFromNetwork;

        protected OnProductUpdateEvent(Product resource, boolean fromNetwork) {
            super(resource);
            isFromNetwork = fromNetwork;
        }

    }

    public class OnProductNotAvailable extends OnResourceEvent<Product> {

        protected OnProductNotAvailable(Product resource) {
            super(resource);
        }

    }

    private static final String PRIVATE_PREFERENCE = "Shopelia$ProductAPI.PrivatePreference";
    private static final String PREFS_PRODUCT = "product:products";

    private static final long KEEP_ALIVE = 10 * TimeUnits.MILISECONDS;

    private static final long POLLING_FREQUENCY = TimeUnits.SECONDS / 2;
    private static final long POLLING_EXPIRATION = 20 * TimeUnits.SECONDS;

    private HttpGetPoller mPoller;
    private SharedPreferences mPreferences;
    private ArrayList<ExtendedProduct> mProducts;
    private ExtendedProduct mProduct;

    private static final Class<?>[] sEventTypes = new Class<?>[] {
            OnProductNotAvailable.class, OnProductUpdateEvent.class
    };

    public ProductAPI(Context context) {
        super(context);
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
            getEventBus().post(new OnProductUpdateEvent(mProduct.getProduct(), false));
        }
        return true;
    }

    public Product getProduct() {
        return mProduct.getProduct();
    }

    @Override
    public Class<?>[] getEventTypes() {
        return sEventTypes;
    }

    private OnPollerEventListener<HttpGetResponse> mOnPollerEventListener = new OnPollerEventListener<HttpGetPoller.HttpGetResponse>() {

        @Override
        public void onTimeExpired() {
            getEventBus().post(new OnProductNotAvailable(mProduct.getProduct()));
        }

        @Override
        public boolean onResult(HttpGetResponse previousResult, final HttpGetResponse newResult) {
            if (newResult.exception != null) {
                fireError(newResult.response, null, newResult.exception);
            } else if (newResult.response != null) {
                try {
                    mProduct.setJson(new JSONObject(newResult.response.getBodyAsString()));
                    mProduct.download_at = System.currentTimeMillis();
                    mProducts.add(mProduct);
                    saveProducts(mProducts);
                    if (mProduct.isValid() && mProduct.ready) {
                        getEventBus().post(new OnProductUpdateEvent(mProduct.getProduct(), true));
                    }
                    return mProduct.isValid();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        public void onPollingSucceed() {

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
