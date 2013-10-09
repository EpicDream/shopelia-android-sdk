package com.shopelia.android.remote.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
 * multiple products). Thread safe, non-blocking parsing.
 * 
 * @author Pierre Pollastri
 */
public class ProductAPI extends ApiController {

    public class OnProductUpdateEvent extends OnResourceEvent<Product> {

        public final boolean isFromNetwork;
        public final boolean isDone;

        protected OnProductUpdateEvent(Product resource, boolean fromNetwork, boolean isDone) {
            super(resource);
            isFromNetwork = fromNetwork;
            this.isDone = isDone;
        }

    }

    public class OnNetworkError {

    }

    public class OnProductNotAvailable extends OnResourceEvent<Product> {

        protected OnProductNotAvailable(Product resource) {
            super(resource);
        }

    }

    private static final String PRIVATE_PREFERENCE = "Shopelia$ProductAPI.PrivatePreference";
    private static final String PREFS_PRODUCT = "product:products";

    private static final long KEEP_ALIVE = 20 * TimeUnits.MINUTES;

    private static final long POLLING_FREQUENCY = TimeUnits.SECONDS / 2;
    private static final long POLLING_EXPIRATION = 20 * TimeUnits.SECONDS;
    private static final long POLLING_OPTIONS_EXPIRATION = 4 * TimeUnits.MINUTES;

    private HttpGetPoller mPoller;
    private SharedPreferences mPreferences;
    private ArrayList<ExtendedProduct> mProducts;
    private ExtendedProduct mProduct;
    private CountDownLatch mCacheLoaded = new CountDownLatch(1);

    private static final Class<?>[] sEventTypes = new Class<?>[] {
            OnProductNotAvailable.class, OnProductUpdateEvent.class, OnApiErrorEvent.class
    };

    public ProductAPI(Context context) {
        super(context);
        mPreferences = context.getSharedPreferences(PRIVATE_PREFERENCE, Context.MODE_PRIVATE);
        getEventBus().register(this);
        getEventBus().post(new LoadProductFromCacheEvent());
    }

    public void getProduct(Product product) {
        getEventBus().post(new GetProductEvent(product));
    }

    public void addProductToCache(Product base, JSONObject object) {
        ExtendedProduct p = new ExtendedProduct(base);
        p.setJson(object);
        p.download_at = System.currentTimeMillis();
        addProductToCache(p);
    }

    private void addProductToCache(ExtendedProduct p) {
        synchronized (mProducts) {
            mProducts.add(p);
        }
    }

    public void save() {
        saveProducts(mProducts);
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
                getEventBus().post(new OnNetworkError());
                return true;
            } else if (newResult.response != null) {
                try {
                    mProduct.setJson(new JSONObject(newResult.response.getBodyAsString()));
                    if (mProduct.getProduct() == null) {
                        return false;
                    }
                    mProduct.download_at = System.currentTimeMillis();
                    boolean isDone = mProduct.isValid() && mProduct.ready
                            && (!mProduct.getProduct().hasVersion() || mProduct.optionsCompleted);

                    if (mProduct.isValid() && mProduct.ready) {
                        getEventBus().postSticky(new OnProductUpdateEvent(mProduct.getProduct(), true, isDone));
                    }
                    if (mProduct.ready && mProduct.getProduct().hasVersion()) {
                        mPoller.setExpiryDuration(POLLING_OPTIONS_EXPIRATION);
                    }

                    if (isDone) {
                        addProductToCache(mProduct);
                        saveProducts(mProducts);
                    }
                    return isDone;
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
        synchronized (mProducts) {
            int count = mProducts.size();
            for (int index = 0; index < count; index++) {
                ExtendedProduct product = mProducts.get(index);
                if (product.url.equals(url)) {
                    if (product.download_at + KEEP_ALIVE < System.currentTimeMillis() || !product.isValid()) {
                        mProducts.remove(index);
                        index -= 1;
                        count -= 1;
                        continue;
                    }
                    saveProducts(mProducts);
                    return product;
                }
            }
            return null;
        }
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
        mCacheLoaded.countDown();
    }

    private void saveProducts(List<ExtendedProduct> products) {
        synchronized (products) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(PREFS_PRODUCT, JsonUtils.toJson(products).toString());
            editor.commit();
        }
    }

    // Private Events
    private class GetProductEvent {
        public final Product product;

        public GetProductEvent(Product product) {
            this.product = product;
        }
    }

    private class LoadProductFromCacheEvent {

    }

    public void onEventAsync(GetProductEvent event) {
        try {
            mCacheLoaded.await();
        } catch (InterruptedException e) {
            // Do nothing
        }
        Product product = event.product;
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
        } else {
            getEventBus().postSticky(new OnProductUpdateEvent(mProduct.getProduct(), false, mProduct.isValid()));
        }
    }

    public void onEventAsync(LoadProductFromCacheEvent event) {
        loadProductsFromCache();
    }

}
