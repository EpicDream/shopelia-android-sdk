package com.shopelia.android.remote.api;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.http.AbstractPoller.OnPollerEventListener;
import com.shopelia.android.http.HttpGetPoller;
import com.shopelia.android.http.HttpGetPoller.HttpGetRequest;
import com.shopelia.android.http.HttpGetPoller.HttpGetResponse;
import com.shopelia.android.model.ExtendedProduct;
import com.shopelia.android.model.Product;
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

		protected OnProductUpdateEvent(Product resource, boolean fromNetwork,
				boolean isDone) {
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

	private static final long KEEP_ALIVE = 20 * TimeUnits.MINUTES;

	private static final long POLLING_FREQUENCY = TimeUnits.SECONDS / 2;
	private static final long POLLING_EXPIRATION = 20 * TimeUnits.SECONDS;
	private static final long POLLING_OPTIONS_EXPIRATION = 4 * TimeUnits.MINUTES;

	private static final String CACHE_DIR = "shopelia/products";
	private static final long DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5 Mo

	private HttpGetPoller mPoller;
	private ExtendedProduct mProduct;
	private Cache mCache;

	private static final Class<?>[] sEventTypes = new Class<?>[] {
			OnProductNotAvailable.class, OnProductUpdateEvent.class,
			OnApiErrorEvent.class };

	public ProductAPI(Context context) {
		super(context);
		mCache = new Cache(new File(context.getCacheDir(), CACHE_DIR),
				KEEP_ALIVE, DISK_CACHE_SIZE);
		getEventBus().register(this);
		getEventBus().post(new LoadProductFromCacheEvent());
	}

	public void getProduct(Product product) {
		getEventBus().post(new GetProductEvent(product));
	}

	public void addProductToCache(Product base, JSONObject object) {
		ExtendedProduct p = new ExtendedProduct(base);
		p.setJson(object);
		addProductToCache(p);
	}

	private void addProductToCache(ExtendedProduct p) {
		p.download_at = System.currentTimeMillis();
		try {
			StringReader reader = new StringReader(p.toJson().toString());
			mCache.write(p.url, reader);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		if (mPoller != null) {
			mPoller.stop();
		}
	}

	@Override
	public Class<?>[] getEventTypes() {
		return sEventTypes;
	}

	private OnPollerEventListener<HttpGetResponse> mOnPollerEventListener = new OnPollerEventListener<HttpGetPoller.HttpGetResponse>() {

		@Override
		public void onTimeExpired() {
			getEventBus()
					.post(new OnProductNotAvailable(mProduct.getProduct()));
		}

		@Override
		public boolean onResult(HttpGetResponse previousResult,
				final HttpGetResponse newResult) {
			if (newResult.exception != null) {
				getEventBus().post(new OnNetworkError());
				return true;
			} else if (newResult.response != null) {
				try {
					mProduct.setJson(new JSONObject(newResult.response
							.getBodyAsString()));
					if (mProduct.getProduct() == null) {
						return false;
					}
					mProduct.download_at = System.currentTimeMillis();
					boolean isDone = mProduct.isValid()
							&& mProduct.ready
							&& (!mProduct.getProduct().hasVersion() || mProduct.optionsCompleted);

					if (mProduct.isValid() && mProduct.ready) {
						getEventBus().postSticky(
								new OnProductUpdateEvent(mProduct.getProduct(),
										true, isDone));
					}
					if (mProduct.ready && mProduct.getProduct().hasVersion()) {
						mPoller.setExpiryDuration(POLLING_OPTIONS_EXPIRATION);
					}
					if (isDone) {
						addProductToCache(mProduct);
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
		if (mCache.exists(url)) {
			StringWriter cachedProduct = new StringWriter();
			try {
				mCache.read(url, cachedProduct);
				ExtendedProduct product = ExtendedProduct
						.inflate(new JSONObject(cachedProduct.toString()));
				return System.currentTimeMillis() - product.download_at < KEEP_ALIVE ? product
						: null;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
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
		Product product = event.product;
		if (product != null && product.isValid() && product.isFromSaturn()) {
			product = new Product(product.url);
		}
		mProduct = new ExtendedProduct(product);
		ExtendedProduct fromCache = findProductByUrl(product.url);
		if (fromCache != null && !product.isValid()) {
			mProduct = fromCache;
		}
		if (mProduct.getProduct() == null
				|| !mProduct.getProduct().isValid()
				|| (mProduct.getProduct().isValid() && mProduct.getProduct()
						.isFromSaturn())) {
			if (mPoller != null) {
				mPoller.stop();
			}
			ShopeliaRestClient client = ShopeliaRestClient.V1(getContext());
			ParameterMap map = client.newParams();
			map.add(Product.Api.URL, mProduct.url);
			mPoller = new HttpGetPoller(client);
			mPoller.setExpiryDuration(POLLING_EXPIRATION)
					.setRequestFrequency(POLLING_FREQUENCY)
					.setParam(new HttpGetRequest(Command.V1.Products.$, map))
					.setOnPollerEventListener(mOnPollerEventListener).poll();
		} else {
			getEventBus().postSticky(
					new OnProductUpdateEvent(mProduct.getProduct(), false,
							mProduct.isValid()));
		}
	}

	public void onEventAsync(LoadProductFromCacheEvent event) {

	}

}
