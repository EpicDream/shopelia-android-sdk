package com.shopelia.android.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;

/**
 * Base fragment class used by the Shopeliad SDK
 * 
 * @author Pierre Pollastri
 */
public class ShopeliaFragment<Contract> extends Fragment {

    private Contract mContract;

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

}
