package com.shopelia.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.model.Product;

public class ProductInformationsFragment extends ShopeliaFragment<Void> {

	public static final class OnInformationsChanged {
		public final String informations;

		private OnInformationsChanged(String informations) {
			this.informations = informations;
		}

	}

	public static final String TAG = "ProductInformations";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(
				R.layout.shopelia_product_informations_fragment, container,
				false);
	}

	public String getInformations() {
		TextView informations = findViewById(R.id.informations);
		return informations != null ? informations.getText().toString() : null;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Product product = (Product) getActivityEventBus().getStickyEvent(
				Product.class);
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivityEventBus().registerSticky(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivityEventBus().unregister(this);
	}

	// Events

	public void onEventMainThread(Product product) {

	}

	// Spinners

}
