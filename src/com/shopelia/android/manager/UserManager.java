package com.shopelia.android.manager;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest.permission;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

import com.shopelia.android.app.Tracker;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.UserAPI;
import com.shopelia.android.utils.ContextUtils;

public class UserManager {

    private static final String USER_MANAGER_PRIVATE_PREFERENCE = "Shopelia$UserManager.PrivatePreference";
    private static final String PREFS_USER_JSON = "user:json";
    private static final String PREFS_AUTH_TOKEN = "user:authToken";
    private static final String PREFS_LOGINS_COUNT = "user:loginsCount";
    private static final String PREFS_CHECKOUT_COUNT = "user:checkoutCount";
    private static final String PREFS_AUTO_SIGNIN = "user:autoSignIn";
    private static final String LOG_TAG = "USER_MANAGER";

    private static UserManager sInstance = null;

    private final Context mContext;
    private final SharedPreferences mPreferences;

    private User mUser;
    private Account mAccount;

    private UserManager(Context context) {
        mContext = context.getApplicationContext();
        mPreferences = context.getSharedPreferences(USER_MANAGER_PRIVATE_PREFERENCE, Context.MODE_PRIVATE);

        final String json = mPreferences.getString(PREFS_USER_JSON, null);
        if (json != null) {
            try {
                mUser = User.inflate(new JSONObject(json));
            } catch (JSONException e) {
                Log.e("Shopelia", "Impossible to restore user from\n" + json, e);
            }
        }
    }

    public static synchronized UserManager get(Context context) {
        if (sInstance == null) {
            sInstance = new UserManager(context);
        }
        return sInstance;
    }

    public void login(User user, String authToken) {
        if (user == null) {
            return;
        }
        setAuthToken(authToken);
        Tracker.Factory.getDefault(mContext).identify(user);
        Tracker.Factory.getDefault(mContext).flush();
        Editor editor = mPreferences.edit();
        editor.putInt(PREFS_LOGINS_COUNT, mPreferences.getInt(PREFS_LOGINS_COUNT, 0) + 1);
        editor.commit();
        mUser = user;
        addAccount(user, authToken);
        saveUser();
    }

    public void update(User user) {
        if (user != null) {
            mUser = user;
            saveUser();
        }
    }

    public void notifyCheckoutSucceed() {
        Editor editor = mPreferences.edit();
        editor.putInt(PREFS_CHECKOUT_COUNT, getCheckoutCount() + 1);
        editor.commit();
    }

    public void saveUser() {
        try {
            Editor editor = mPreferences.edit();
            editor.putString(PREFS_USER_JSON, mUser.toJson().toString());
            editor.commit();
            AccountManager manager = getAccountManager();
            if (manager != null) {

            }
        } catch (JSONException e) {
            if (Config.ERROR_LOGS_ENABLED) {
                Log.e("Shopelia", "Impossible to save user", e);
            }
        }
    }

    public int getLoginsCount() {
        return mPreferences.getInt(PREFS_LOGINS_COUNT, 0);
    }

    public int getCheckoutCount() {
        return mPreferences.getInt(PREFS_CHECKOUT_COUNT, 0);
    }

    public void setAuthToken(String token) {
        Editor editor = mPreferences.edit();
        editor.putString(PREFS_AUTH_TOKEN, token);
        editor.commit();
    }

    public String getAuthToken() {
        return mPreferences.getString(PREFS_AUTH_TOKEN, null);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void restoreFromAccount(AccountManagerCallback<Bundle> callback) {
        if (hasAccountPermission() && getAccount() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getAccountManager().getAuthToken(getAccount(), Config.AUTH_TOKEN_TYPE, null, false, callback, null);
            } else {
                getAccountManager().getAuthToken(getAccount(), Config.AUTH_TOKEN_TYPE, false, callback, null);
            }
        }
    }

    public void logout() {
        if (mUser != null) {
            removeAccount();
            new UserAPI(mContext, null).signOut(mUser.email);
            mUser = null;
            Editor editor = mPreferences.edit();
            editor.remove(PREFS_AUTH_TOKEN);
            editor.remove(PREFS_USER_JSON);
            editor.commit();
            Tracker.Factory.getDefault(mContext).unidentify();
            Tracker.Factory.getDefault(mContext).flush();
            setAutoSignIn(false);
        }
    }

    public Account getAccount() {
        if (mAccount != null) {
            return mAccount;
        }
        Account[] accounts = getAccounts();
        if (accounts == null) {
            return null;
        }
        for (int index = 0; index < accounts.length; index++) {
            if (accounts[index] != null) {
                mAccount = accounts[index];
                return accounts[index];
            }
        }
        return null;
    }

    private void addAccount(User user, String authToken) {
        Account[] accounts = getAccounts();
        AccountManager manager = getAccountManager();

        if (manager == null) {
            return;
        }

        if (accounts == null || accounts.length > 0) {
            return;
        }
        final Account account = new Account(mUser.email, Config.ACCOUNT_TYPE);
        manager.addAccountExplicitly(account, null, mUser.toUserdata(authToken));

        if (Config.INFO_LOGS_ENABLED) {
            Log.i(LOG_TAG, "Adding a Shopelia Account");
        }

    }

    private void removeAccount() {
        AccountManager manager = getAccountManager();
        if (getAccount() != null) {
            if (Config.INFO_LOGS_ENABLED) {
                Log.i(LOG_TAG, "Removing a Shopelia Account");
            }
            manager.removeAccount(getAccount(), null, null);
        }
        mAccount = null;
    }

    public Account[] getAccounts() {
        AccountManager manager = getAccountManager();
        if (manager == null) {
            return null;
        }
        return manager.getAccountsByType(Config.ACCOUNT_TYPE);
    }

    public AccountManager getAccountManager() {
        return hasAccountPermission() ? AccountManager.get(mContext) : null;
    }

    public boolean hasAccountPermission() {
        return ContextUtils.hasPermissions(mContext, permission.MANAGE_ACCOUNTS, permission.USE_CREDENTIALS, permission.GET_ACCOUNTS,
                permission.AUTHENTICATE_ACCOUNTS);
    }

    public boolean isLogged() {
        return getAuthToken() != null;
    }

    public boolean isAutoSignedIn() {
        return mPreferences.getBoolean(PREFS_AUTO_SIGNIN, false);
    }

    public void revokeAutoSignIn() {
        setAutoSignIn(false);
    }

    public void setAutoSignIn(boolean value) {
        Editor edit = mPreferences.edit();
        edit.putBoolean(PREFS_AUTO_SIGNIN, value);
        edit.commit();
    }

    public User getUser() {
        return isLogged() ? mUser : null;
    }
}
