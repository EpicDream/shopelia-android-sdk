package com.shopelia.android.app;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.shopelia.android.app.ShopeliaDialog.DialogCallback;
import com.shopelia.android.app.tracking.Tracker;
import com.shopelia.android.model.Order;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;

import de.greenrobot.event.EventBus;

/**
 * Base fragment class used by the Shopeliad SDK
 * 
 * @author Pierre Pollastri
 */
public class ShopeliaFragment<Contract> extends DialogFragment {

	private Contract mContract;

	public interface SafeContextOperation {
		public void run(ShopeliaActivity activity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContract = (Contract) activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		onAttach();
	}

	public void onAttach() {
		if (!getShowsDialog()) {
			onCreateShopeliaActionBar(getBaseActivity().getShopeliaActionBar());
		}
	}

	public EventBus getActivityEventBus() {
		if (getBaseActivity() != null) {
			return getBaseActivity().getEventBus();
		}
		return EventBus.getDefault();
	}

	@Override
	public void show(FragmentManager manager, String tag) {
		super.show(manager, tag);
	}

	protected void onCreateShopeliaActionBar(ActionBar actionBar) {

	}

	protected void onActionItemSelected(Item item) {

	}

	protected ShopeliaDialog getShopeliaDialog() {
		if (getShowsDialog() && getDialog() instanceof ShopeliaDialog) {
			return (ShopeliaDialog) getDialog();
		}
		return null;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ShopeliaDialog dialog = new ShopeliaDialog(getActivity());
		dialog.setDialogCallback(new DialogCallback() {

			@Override
			public void onCreateShopeliaActionBar(ShopeliaDialog dialog,
					ActionBar actionBar) {
				ShopeliaFragment.this.onCreateShopeliaActionBar(actionBar);
			}

			@Override
			public void onActionItemSelected(ShopeliaDialog dialog, Item item) {
				ShopeliaFragment.this.onActionItemSelected(item);
			}
		});
		return dialog;
	}

	public Tracker getTracker() {
		ShopeliaActivity activity = (ShopeliaActivity) getContract();
		if (activity != null) {
			return activity.getTracker();
		}
		return Tracker.Factory.create(Tracker.PROVIDER_DUMMY);
	}

	public void fireScreenSeenEvent(final String screenName) {
		runSafely(new SafeContextOperation() {

			@Override
			public void run(ShopeliaActivity activity) {
				activity.fireScreenSeenEvent(screenName);
			}
		});
	}

	public void closeSoftKeyboard() {
		runSafely(new SafeContextOperation() {

			@Override
			public void run(ShopeliaActivity activity) {
				activity.closeSoftKeyboard();
			}
		});
	}

	public void startWaiting(CharSequence message, boolean blockUi,
			boolean isCancelable) {
		if (!getShowsDialog()) {
			ShopeliaActivity activity = (ShopeliaActivity) mContract;
			if (activity != null) {
				activity.startWaiting(message, blockUi, isCancelable);
			}
		} else {
			getShopeliaDialog().startWaiting(message, blockUi, isCancelable);
		}
	}

	public Order getOrder() {
		if (getBaseActivity() != null) {
			return getBaseActivity().getOrder();
		}
		return null;
	}

	public void stopWaiting() {
		if (!getShowsDialog()) {
			ShopeliaActivity activity = (ShopeliaActivity) mContract;
			if (activity != null) {
				activity.stopWaiting();
			}
		} else {
			getShopeliaDialog().stopWaiting();
		}
	}

	public Contract getContract() {
		return mContract;
	}

	@SuppressWarnings("unchecked")
	public <T extends ShopeliaActivity> T getBaseActivity() {
		return (T) getActivity();
	}

	@SuppressWarnings("unchecked")
	public <T extends View> T findViewById(int id) {
		if (getView() == null) {
			return null;
		}
		return (T) getView().findViewById(id);
	}

	@SuppressWarnings("unchecked")
	public <T extends View> T findViewById(int id, Class<T> clazz) {
		if (getView() == null) {
			return null;
		}
		View view = getView().findViewById(id);
		return (T) view;
	}

	public void runSafely(SafeContextOperation operation) {
		ShopeliaActivity activity = (ShopeliaActivity) mContract;
		if (activity != null) {
			operation.run(activity);
		}
	}

	public String getName() {
		return null;
	}

	public void replace(FragmentManager fragmentManager, int resId) {
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(resId, this);
		ft.commit();
	}

	public <T extends Fragment> T findFragmentByTag(String tag) {
		FragmentManager fm = getChildFragmentManager();
		return (T) fm.findFragmentByTag(tag);
	}

}
