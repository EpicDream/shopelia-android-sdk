package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.AuthenticateFragment.OnAuthenticateEvent;
import com.shopelia.android.AuthenticateFragment.OnLogoutEvent;
import com.shopelia.android.ProductSelectionCardFragment.OnQuantitiySelectedEvent;
import com.shopelia.android.ProductSelectionCardFragment.OnSubmitProductEvent;
import com.shopelia.android.app.CardHolderActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.image.ImageLoader;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Option;
import com.shopelia.android.model.Product;
import com.shopelia.android.remote.api.MerchantsAPI;
import com.shopelia.android.remote.api.ProductAPI;
import com.shopelia.android.remote.api.ProductAPI.OnNetworkError;
import com.shopelia.android.remote.api.ProductAPI.OnProductUpdateEvent;

public class ProductActivity extends CardHolderActivity {

	/**
	 * Url of the product to purchase
	 */
	public static final String EXTRA_PRODUCT_URL = Config.EXTRA_PREFIX
			+ "PRODUCT_URL";

	public static final String EXTRA_PRODUCT = Config.EXTRA_PREFIX + "PRODUCT";

	public static final String ACTIVITY_NAME = "Product";

	private ProductAPI mProductAPI;
	private boolean mHasProductSummary = false;
	private boolean mHasProductSelection = false;
	private Option[] mCurrentOptions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// This is the activity to be called by the SDK. It must init the order
		// for the rest of the Workflow
		getIntent().putExtra(EXTRA_INIT_ORDER, true);
		setActivityStyle(STYLE_FULLSCREEN);
		super.onCreate(savedInstanceState);
		mProductAPI = new ProductAPI(this);
		ImageLoader.get(this).flush();
		if (savedInstanceState != null) {
			getOrder().product = null;
			removeCardNow(ProductSelectionCardFragment.TAG);
			removeCardNow(ProductSummaryCardFragment.TAG);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mProductAPI.registerSticky(this);
		if (getIntent().hasExtra(EXTRA_PRODUCT)) {
			getEventBus().post(new ProductNotFoundFragment.DismissEvent());
			getEventBus().post(new ErrorCardFragment.DismissEvent());
			startWaiting(getString(R.string.shopelia_product_loading), false,
					true);
			getOrder().product = getIntent().getParcelableExtra(EXTRA_PRODUCT);
			mProductAPI.getProduct(getOrder().product);
		} else if (getOrder().product == null || !getOrder().product.isValid()) {
			getEventBus().post(new ProductNotFoundFragment.DismissEvent());
			getEventBus().post(new ErrorCardFragment.DismissEvent());
			startWaiting(getString(R.string.shopelia_product_loading), false,
					true);
			mProductAPI.getProduct(new Product(getIntent().getExtras()
					.getString(EXTRA_PRODUCT_URL)));
		} else {
			stopWaiting();
			getEventBus().postSticky(getOrder().product);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mProductAPI.unregister(this);
		mProductAPI.stop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_LOGOUT) {
			finish();
			return;
		}
		switch (requestCode) {
		case REQUEST_CHECKOUT:
			if (resultCode == RESULT_OK) {
				setResult(RESULT_OK);
				finish();
			} else {

			}
			break;

		default:
			break;
		}
	}

	@Override
	public String getActivityName() {
		return ACTIVITY_NAME;
	}

	public void onEventMainThread(OnProductUpdateEvent event) {
		if (mCurrentOptions == null) {
			mCurrentOptions = event.resource.getCurrentVersion().getOptions();
		}
		int quantity = getOrder().product != null ? getOrder().product
				.getQuantity() : 1;
		getEventBus().post(new ProductNotFoundFragment.DismissEvent());
		getEventBus().post(new ErrorCardFragment.DismissEvent());
		getOrder().product = event.resource;
		if (event.isDone) {
			stopWaiting();
		}
		if (mCurrentOptions != null) {
			getOrder().product.setCurrentVersion(mCurrentOptions);
		}
		getOrder().product.setQuantity(quantity);
		if (!mHasProductSummary && event.resource.hasVersion()) {
			mHasProductSummary = true;
			addCard(new ProductSummaryCardFragment(), 0, false,
					ProductSummaryCardFragment.TAG);
		}
		if (!mHasProductSelection && event.resource.hasVersion()) {
			mHasProductSelection = true;
			addCard(ProductSelectionCardFragment.newInstance(event.resource),
					0, false, ProductSelectionCardFragment.TAG);
		}
		getEventBus().postSticky(event.resource);
	}

	public void onEventMainThread(ProductAPI.OnProductNotAvailable event) {
		stopWaiting();
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Product p = new Product(getIntent().getExtras().getString(
				EXTRA_PRODUCT_URL));
		p.merchant = new MerchantsAPI(this).getMerchant(p.url);
		ft.replace(R.id.overlay_frame, ProductNotFoundFragment.newInstance(p));
		ft.commit();
	}

	public void onEventMainThread(OnNetworkError event) {
		getEventBus().post(new ProductNotFoundFragment.DismissEvent());
		stopWaiting();
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.overlay_frame, ErrorCardFragment.newInstance(
				getString(R.string.shopelia_error_no_network),
				getString(R.string.shopelia_error_no_network_button)));
		ft.commit();
	}

	public void onEventMainThread(ProductOptionsFragment.OnOptionsChanged event) {
		if (getOrder().product != null) {
			getOrder().product.setCurrentVersion(event.lastChange,
					event.options);
			mCurrentOptions = getOrder().product.getCurrentVersion()
					.getOptions();
			getEventBus().postSticky(getOrder().product);
		}
	}

	public void onEventMainThread(OnSubmitProductEvent event) {
		getOrder().informations = event.informations;
		if (UserManager.get(this).isLogged()) {
			new AuthenticateFragment().show(getSupportFragmentManager(), null);
		} else {
			Intent intent = new Intent(this, PrepareOrderActivity.class);
			intent.putExtra(EXTRA_ORDER, getOrder());
			startActivityForResult(intent, REQUEST_CHECKOUT);
		}
	}

	public void onEventMainThread(
			ErrorCardFragment.OnErrorButtonClickEvent event) {
		getEventBus().post(new ProductNotFoundFragment.DismissEvent());
		getEventBus().post(new ErrorCardFragment.DismissEvent());
		startWaiting(getString(R.string.shopelia_product_loading), false, true);
		mProductAPI.getProduct(new Product(getIntent().getExtras().getString(
				EXTRA_PRODUCT_URL)));
	}

	public void onEventMainThread(OnAuthenticateEvent event) {
		UserManager.get(this).setAutoSignIn(event.autoSignIn);
		Intent intent = new Intent(this, ProcessOrderActivity.class);
		intent.putExtra(EXTRA_ORDER, getOrder());
		startActivityForResult(intent, REQUEST_CHECKOUT);
	}

	public void onEventMainThread(OnLogoutEvent event) {
		Intent intent = new Intent(this, PrepareOrderActivity.class);
		intent.putExtra(EXTRA_ORDER, getOrder());
		startActivityForResult(intent, REQUEST_CHECKOUT);
	}

	public void onEventMainThread(OnQuantitiySelectedEvent event) {
		getOrder().product.setQuantity(event.quantity);
		getEventBus().postSticky(getOrder().product);
	}

}
