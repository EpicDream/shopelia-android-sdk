package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;

import com.shopelia.android.ProductSelectionCardFragment.OnSubmitProductEvent;
import com.shopelia.android.app.CardHolderActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.Option;
import com.shopelia.android.model.Product;
import com.shopelia.android.remote.api.ProductAPI;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This is the activity to be called by the SDK. It must init the order
        // for the rest of the Workflow
        getIntent().putExtra(EXTRA_INIT_ORDER, true);
        setActivityStyle(STYLE_FULLSCREEN);
        super.onCreate(savedInstanceState);
        mProductAPI = new ProductAPI(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProductAPI.registerSticky(this);
        if (mProduct == null || !mProduct.isValid()) {
            startWaiting(getString(R.string.shopelia_product_loading), false, true);
            mProductAPI.getProduct(new Product(getIntent().getExtras().getString(EXTRA_PRODUCT_URL)));
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
        mProduct = event.resource;
        if (event.isDone) {
            stopWaiting();
        }
        if (mCurrentOptions != null) {
            mProduct.setCurrentVersion(mCurrentOptions);
        }
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

    public void onEventMainThread(ProductOptionsFragment.OnOptionsChanged event) {
        if (mProduct != null) {
            mProduct.setCurrentVersion(event.lastChange, event.options);
            mCurrentOptions = mProduct.getCurrentVersion().getOptions();
            getEventBus().postSticky(mProduct);
        }
    }

    public void onEventMainThread(OnSubmitProductEvent event) {
        getOrder().product = mProduct;
        Intent intent = new Intent(this, PrepareOrderActivity.class);
        intent.putExtra(EXTRA_ORDER, getOrder());
        startActivityForResult(intent, REQUEST_CHECKOUT);
    }

}
