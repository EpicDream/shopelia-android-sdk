package com.shopelia.android.app;

/**
 * A special {@link ShopeliaFragment} with generic methods in order to deal with
 * {@link AccountAuthenticatorShopeliaActivity}
 * 
 * @author Pierre Pollastri
 * @param <Contract>
 */
public class AccountAuthenticatorShopeliaFragment<Contract> extends ShopeliaFragment<Contract> {

    public AccountAuthenticatorShopeliaActivity getAuthenticatorActivity() {
        if (getActivity() instanceof AccountAuthenticatorShopeliaActivity) {
            return (AccountAuthenticatorShopeliaActivity) getActivity();
        }
        return null;
    }

    public boolean isCalledByAcountManager() {
        if (getAuthenticatorActivity() != null) {
            return getAuthenticatorActivity().isCalledByAcountManager();
        }
        return false;
    }

}
