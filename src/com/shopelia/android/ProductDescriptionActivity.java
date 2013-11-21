package com.shopelia.android;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.webkit.WebView;

import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.Product;

public class ProductDescriptionActivity extends ShopeliaActivity {

	public static final String EXTRA_PRODUCT = Config.EXTRA_PREFIX + "PRODUCT";

	private WebView mWebView;
	private Product mProduct;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHostContentView(R.layout.shopelia_product_description_activity);
		mWebView = (WebView) findViewById(R.id.description_content);
        getProduct();
		mWebView.loadDataWithBaseURL(null,
				getProduct().getCurrentVersion().description, "text/html",
				"utf-8", null);

		ProductSummaryCardFragment summaryCardFragment = ProductSummaryCardFragment
				.newInstance(false);
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.summary, summaryCardFragment);
		ft.commit();
		getEventBus().postSticky(getProduct());
	}

	@Override
	public String getActivityName() {
		return "Product Description";
	}

	@Override
	protected boolean isPartOfOrderWorkFlow() {
		return false;
	}

	private Product getProduct() {
		if (mProduct == null) {
			mProduct = getIntent().getParcelableExtra(EXTRA_PRODUCT);
		}
		return mProduct;
	}

}
