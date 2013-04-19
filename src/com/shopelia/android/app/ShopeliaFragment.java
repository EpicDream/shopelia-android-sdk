package com.shopelia.android.app;

import android.support.v4.app.Fragment;

/**
 * Base fragment class used by the Shopeliad SDK
 * 
 * @author Pierre Pollastri
 */
public class ShopeliaFragment<Contract> extends Fragment {

    @SuppressWarnings("unchecked")
    public Contract getContract() {
        return (Contract) getActivity();
    }

    @SuppressWarnings("unchecked")
    public <T extends HostActivity> T getBaseActivity() {
        return (T) getActivity();
    }

}
