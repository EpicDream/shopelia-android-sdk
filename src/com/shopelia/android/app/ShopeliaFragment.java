package com.shopelia.android.app;

import android.app.Activity;
import android.support.v4.app.Fragment;

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

    public Contract getContract() {
        return mContract;
    }

    @SuppressWarnings("unchecked")
    public <T extends HostActivity> T getBaseActivity() {
        return (T) getActivity();
    }

}
