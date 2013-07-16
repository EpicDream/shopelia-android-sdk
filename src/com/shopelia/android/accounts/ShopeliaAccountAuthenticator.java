package com.shopelia.android.accounts;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.User;

public class ShopeliaAccountAuthenticator extends AbstractAccountAuthenticator {

    private Context mContext;
    private AccountManager mManager;

    public ShopeliaAccountAuthenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures,
            Bundle options) throws NetworkErrorException {
        Log.d(null, "Add Account");
        final Intent intent = new Intent(mContext, PrepareOrderActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;

    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {

        // If the caller requested an authToken type we don't support, then
        // return an error
        if (!authTokenType.equals(Config.AUTH_TOKEN_TYPE)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        if (account != null && getAccountManager() != null) {
            Bundle result = new Bundle();
            String auth_token = getAccountManager().getUserData(account, User.Api.AUTH_TOKEN);
            String user = getAccountManager().getUserData(account, User.Api.USER);
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, Config.ACCOUNT_TYPE);
            result.putString(AccountManager.KEY_AUTHTOKEN, auth_token);
            result.putString(AccountManager.KEY_USERDATA, user);
            return result;
        } else {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        return null;
    }

    public AccountManager getAccountManager() {
        if (mManager == null) {
            mManager = AccountManager.get(mContext);
        }
        return mManager;
    }
}
