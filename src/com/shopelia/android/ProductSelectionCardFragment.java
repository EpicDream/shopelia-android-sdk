package com.shopelia.android;

import java.math.BigDecimal;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.shopelia.android.app.CardFragment;
import com.shopelia.android.model.Product;
import com.shopelia.android.model.Version;

public class ProductSelectionCardFragment extends CardFragment {

	public class OnSubmitProductEvent {
		public final String informations;

		private OnSubmitProductEvent(String informations) {
			this.informations = informations;
		}

	}

	public class OnQuantitiySelectedEvent {
		public final int quantity;

		private OnQuantitiySelectedEvent(int quantity) {
			this.quantity = quantity;
		}

	}

	public static final String TAG = "Product Selection";

	private static final String ARGS_PRODUCT = "args:product";

	private static final int MAX_QUANTITY = 4;

	private Product mProduct;
	private ProductOptionsFragment mOptionsFragment;

	public static ProductSelectionCardFragment newInstance(Product product) {
		ProductSelectionCardFragment fragment = new ProductSelectionCardFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARGS_PRODUCT, product);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.shopelia_product_selection_card,
				container, false);
	}

	@Override
	public void onBindView(View view, Bundle savedInstanceState) {
		super.onBindView(view, savedInstanceState);
		mProduct = getArguments().getParcelable(ARGS_PRODUCT);

		if (mProduct.merchant.allowQuantities) {
			Spinner s = findViewById(R.id.quantitiy_selector);
			Integer[] items = new Integer[MAX_QUANTITY];
			for (int index = 0; index < MAX_QUANTITY; index++) {
				items[index] = Integer.valueOf(index + 1);
			}
			ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(
					getActivity(),
					android.R.layout.simple_spinner_dropdown_item, items);
			s.setAdapter(adapter);
			s.setOnItemSelectedListener(mOnQuantitySelectedListener);
		} else {
			((View) findViewById(R.id.quantitiy_selector).getParent())
					.setVisibility(View.GONE);
		}

		refreshPrices();
		refreshOptionsFragment();
		findViewById(R.id.validate).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String informations = null;
				ProductInformationsFragment fragment = findFragmentByTag(ProductInformationsFragment.TAG);
				if (fragment != null) {
					informations = fragment.getInformations();
				}
				getActivityEventBus().post(
						new OnSubmitProductEvent(informations));
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivityEventBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivityEventBus().unregister(this);
	}

	// Events
	public void onEventMainThread(Product product) {
		getArguments().putParcelable(ARGS_PRODUCT, product);
		mProduct = product;
		refreshPrices();
		refreshOptionsFragment();
	}

	private void refreshPrices() {
		Spinner s = findViewById(R.id.quantitiy_selector);
		s.setSelection(mProduct.getQuantity() - 1);
		TextView t = findViewById(R.id.product_price_strikeout);
		t.setPaintFlags(t.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		setPriceOrHide(R.id.product_price_strikeout,
				mProduct.getStrikeoutPrice());
		setPriceOrHide(R.id.product_price, mProduct.getSingleProductPrice());
		setPriceOrHide(R.id.delivery_price, mProduct.getCurrentVersion()
				.getShippingPrice(), BigDecimal.ZERO);
		setPriceOrHide(R.id.product_total_price, mProduct.getTotalPrice());
		setMinusPriceOrHide(R.id.price_cashfront,
				mProduct.getExpectedCashfrontValue());
		findViewById(R.id.product_delivery_free_layout)
				.setVisibility(
						mProduct.getShippingPrice().compareTo(BigDecimal.ZERO) <= 0 ? View.VISIBLE
								: View.GONE);
		setTextOrHide(R.id.product_availability_info,
				mProduct.getCurrentVersion().availabilityInfo);
		setTextOrHide(R.id.product_shipping_extra,
				mProduct.getCurrentVersion().shippingExtra);
		TextView quantityMultiply = findViewById(R.id.product_quantity_multiply);
		quantityMultiply.setText(getString(
				R.string.shopelia_confirmation_quantity_multiply,
				mProduct.getQuantity()));
		quantityMultiply
				.setVisibility(mProduct.getQuantity() > 1 ? View.VISIBLE
						: View.GONE);
	}

	private void refreshOptionsFragment() {
		if (mProduct.versions.getOptionsCount() > 0) {
			if (mOptionsFragment == null) {
				mOptionsFragment = new ProductOptionsFragment();
				FragmentManager fm = getChildFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				ft.replace(R.id.option_fragment, mOptionsFragment,
						ProductOptionsFragment.TAG);
				ft.commit();
			}
		}
		if (!mProduct.isFromSaturn()
				&& findFragmentByTag(ProductInformationsFragment.TAG) == null) {
			ProductInformationsFragment fragment = new ProductInformationsFragment();
			FragmentManager fm = getChildFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.information_fragment, fragment,
					ProductInformationsFragment.TAG);
			ft.commit();
		}
	}

	private void setTextOrHide(int id, CharSequence text) {
		if (TextUtils.isEmpty(text)) {
			findViewById(id).setVisibility(View.GONE);
		} else {
			findViewById(id, TextView.class).setText(text);
			findViewById(id).setVisibility(View.VISIBLE);
		}
	}

	private void setPriceOrHide(int id, BigDecimal price) {
		setPriceOrHide(id, price, Version.NO_PRICE);
	}

	private void setPriceOrHide(int id, BigDecimal price,
			BigDecimal failureValue) {
		TextView textView = findViewById(id);
		textView.setText(mProduct.currency.format(price));
		if (price.compareTo(failureValue) == 0) {
			((View) textView.getParent()).setVisibility(View.GONE);
		} else {
			((View) textView.getParent()).setVisibility(View.VISIBLE);
		}
	}

	private void setMinusPriceOrHide(int id, BigDecimal price) {
		TextView textView = findViewById(id);
		textView.setText("-" + mProduct.currency.format(price));
		((View) textView.getParent()).setVisibility(price
				.compareTo(BigDecimal.ZERO) <= 0 ? View.GONE : View.VISIBLE);
	}

	private OnItemSelectedListener mOnQuantitySelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View selected,
				int position, long id) {
			getActivityEventBus().post(
					new OnQuantitiySelectedEvent(position + 1));
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {

		}

	};

}
