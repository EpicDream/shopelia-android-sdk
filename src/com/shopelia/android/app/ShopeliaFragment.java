package com.shopelia.android.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.shopelia.android.app.tracking.DummyTracker;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;

/**
 * Base fragment class used by the Shopeliad SDK
 * 
 * @author Pierre Pollastri
 */
public class ShopeliaFragment<Contract> extends Fragment {

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
        onCreateShopeliaActionBar(getBaseActivity().getShopeliaActionBar());
    }

    protected void onCreateShopeliaActionBar(ActionBar actionBar) {

    }

    protected void onActionItemSelected(Item item) {

    }

    public ShopeliaTracker getTracker() {
        ShopeliaActivity activity = (ShopeliaActivity) getContract();
        if (activity != null) {
            return activity.getTracker();
        }
        return new DummyTracker();
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

    public void startWaiting(CharSequence message, boolean blockUi, boolean isCancelable) {
        ShopeliaActivity activity = (ShopeliaActivity) mContract;
        if (activity != null) {
            activity.startWaiting(message, blockUi, isCancelable);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if (intent != null) {
            intent.putExtra(ShopeliaActivity.EXTRA_SESSION_ID, getBaseActivity().getSessionId());
        }
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (intent != null) {
            intent.putExtra(ShopeliaActivity.EXTRA_SESSION_ID, getBaseActivity().getSessionId());
        }
        super.startActivityForResult(intent, requestCode);
    }

    public void stopWaiting() {
        ShopeliaActivity activity = (ShopeliaActivity) mContract;
        if (activity != null) {
            activity.stopWaiting();
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

}
