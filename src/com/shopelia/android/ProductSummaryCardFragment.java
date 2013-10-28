package com.shopelia.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.app.CardFragment;
import com.shopelia.android.model.Product;
import com.shopelia.android.widget.AsyncImageView;

public class ProductSummaryCardFragment extends CardFragment {

	public static final String TAG = "ProductSummary";

	private static final String ARGS_ALLOW_DESCRIPTION = "allow_description";

	private Product mProduct;
	private TextView mProductTitle;
	private TextView mProductDescription;
	private AsyncImageView mProductImage;
	private TextView mMerchantName;

	public static ProductSummaryCardFragment newInstance(
			boolean allowDescription) {
		ProductSummaryCardFragment fragment = new ProductSummaryCardFragment();
		Bundle args = new Bundle();
		args.putBoolean(ARGS_ALLOW_DESCRIPTION, allowDescription);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.shopelia_product_summary_card,
				container, false);
	}

	@Override
	public void onBindView(View view, Bundle savedInstanceState) {
		super.onBindView(view, savedInstanceState);
		clear(mProductTitle = findViewById(R.id.product_title));
		clear(mProductDescription = findViewById(R.id.product_description));
		clear(mMerchantName = findViewById(R.id.product_merchant_name));
		mProductImage = findViewById(R.id.product_image);
		mMerchantName.setOnClickListener(mOnClickOnMerchantListener);
		findViewById(R.id.product_more).setOnClickListener(
				mOnClickOnMoreListener);
		if (getArguments() != null
				&& !getArguments().getBoolean(ARGS_ALLOW_DESCRIPTION, true)) {
			findViewById(R.id.product_more).setVisibility(View.GONE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivityEventBus().registerSticky(this, Product.class);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivityEventBus().unregister(this);
	}

	// Events
	public void onEventMainThread(Product product) {
		mProduct = product;
		mProductTitle.setText(product.getCurrentVersion().name);
		mProductDescription.setText(Html.fromHtml(
				product.getCurrentVersion().description).toString());
		mProductImage.setUrl(product.getCurrentVersion().imageUrl);
		mMerchantName.setText(product.merchant.name);
	}

	private static TextView clear(TextView tv) {
		tv.setText(null);
		return tv;
	}

	private OnClickListener mOnClickOnMerchantListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(
					R.string.shopelia_product_description_go_merchant_title)
					.setMessage(
							R.string.shopelia_product_description_go_merchant_content);
			builder.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent newIntent = new Intent(Intent.ACTION_VIEW,
									Uri.parse(mProduct.url));
							startActivity(newIntent);
						}
					});
			builder.setNegativeButton(android.R.string.no,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.show();
		}
	};

	private OnClickListener mOnClickOnMoreListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(),
					ProductDescriptionActivity.class);
			intent.putExtra(ProductDescriptionActivity.EXTRA_PRODUCT,
					getOrder().product);
			startActivity(intent);
			getTracker().track("Click On Product Description");
		}
	};

}
