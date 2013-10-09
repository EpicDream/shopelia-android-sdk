package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.AuthenticateFragment.OnAuthenticateEvent;
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
    public static final String EXTRA_PRODUCT_URL = Config.EXTRA_PREFIX + "PRODUCT_URL";

    private static final String SAVE_PRODUCT = "save:product";

    public static final String ACTIVITY_NAME = "Product";

    private ProductAPI mProductAPI;
    private Product mProduct;
    private boolean mHasProductSummary = false;
    private boolean mHasProductSelection = false;
    private Option[] mCurrentOptions;
    private int mQuantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This is the activity to be called by the SDK. It must init the order
        // for the rest of the Workflow
        getIntent().putExtra(EXTRA_INIT_ORDER, true);
        setActivityStyle(STYLE_FULLSCREEN);
        super.onCreate(savedInstanceState);
        mProductAPI = new ProductAPI(this);
        ImageLoader.get(this).flush();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProductAPI.registerSticky(this);
        if (mProduct == null || !mProduct.isValid()) {
            getEventBus().post(new ProductNotFoundFragment.DismissEvent());
            getEventBus().post(new ErrorCardFragment.DismissEvent());
            startWaiting(getString(R.string.shopelia_product_loading), false, true);
            mProductAPI.getProduct(new Product(getIntent().getExtras().getString(EXTRA_PRODUCT_URL)));
        } else {
            stopWaiting();
            getEventBus().postSticky(mProduct);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mProductAPI.unregister(this);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_PRODUCT, mProduct);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mProduct = savedInstanceState.getParcelable(SAVE_PRODUCT);
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

    public void onEventMainThread(OnProductUpdateEvent event) {
        getEventBus().post(new ProductNotFoundFragment.DismissEvent());
        getEventBus().post(new ErrorCardFragment.DismissEvent());
        mProduct = event.resource;
        if (event.isDone) {
            stopWaiting();
        }
        if (mCurrentOptions != null) {
            mProduct.setCurrentVersion(mCurrentOptions);
        }
        mProduct.setQuantity(mQuantity);
        getOrder().product = mProduct;
        if (!mHasProductSummary && event.resource.hasVersion()) {
            mHasProductSummary = true;
            addCard(new ProductSummaryCardFragment(), 0, false, ProductSummaryCardFragment.TAG);
        }
        if (!mHasProductSelection && event.resource.hasVersion()) {
            mHasProductSelection = true;
            addCard(ProductSelectionCardFragment.newInstance(event.resource), 0, false, ProductSelectionCardFragment.TAG);
        }
        getEventBus().postSticky(event.resource);
    }

    public void onEventMainThread(ProductAPI.OnProductNotAvailable event) {
        stopWaiting();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Product p = new Product(getIntent().getExtras().getString(EXTRA_PRODUCT_URL));
        p.merchant = new MerchantsAPI(this).getMerchant(p.url);
        ft.replace(R.id.overlay_frame, ProductNotFoundFragment.newInstance(p));
        ft.commit();
    }

    public void onEventMainThread(OnNetworkError event) {
        getEventBus().post(new ProductNotFoundFragment.DismissEvent());
        stopWaiting();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.overlay_frame, ErrorCardFragment.newInstance(getString(R.string.shopelia_error_no_network),
                getString(R.string.shopelia_error_no_network_button)));
        ft.commit();
    }

    public void onEventMainThread(ProductOptionsFragment.OnOptionsChanged event) {
        if (mProduct != null) {
            mProduct.setCurrentVersion(event.lastChange, event.options);
            mCurrentOptions = mProduct.getCurrentVersion().getOptions();
            getEventBus().postSticky(mProduct);
        }
    }

    public void onEventMainThread(OnSubmitProductEvent event) {
        if (UserManager.get(this).isLogged()) {
            new AuthenticateFragment().show(getSupportFragmentManager(), null);
        } else {
            Intent intent = new Intent(this, PrepareOrderActivity.class);
            intent.putExtra(EXTRA_ORDER, getOrder());
            startActivityForResult(intent, REQUEST_CHECKOUT);
        }
    }

    public void onEventMainThread(ErrorCardFragment.OnErrorButtonClickEvent event) {
        getEventBus().post(new ProductNotFoundFragment.DismissEvent());
        getEventBus().post(new ErrorCardFragment.DismissEvent());
        startWaiting(getString(R.string.shopelia_product_loading), false, true);
        mProductAPI.getProduct(new Product(getIntent().getExtras().getString(EXTRA_PRODUCT_URL)));
    }

    public void onEventMainThread(OnAuthenticateEvent event) {
        UserManager.get(this).setAutoSignIn(event.autoSignIn);
        Intent intent = new Intent(this, ProcessOrderActivity.class);
        intent.putExtra(EXTRA_ORDER, getOrder());
        startActivityForResult(intent, REQUEST_CHECKOUT);
    }

    public void onEventMainThread(OnQuantitiySelectedEvent event) {
        mProduct.setQuantity(event.quantity);
        getEventBus().postSticky(mProduct);
    }

}
