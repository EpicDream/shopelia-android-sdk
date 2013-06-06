package com.shopelia.android.manager;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.shopelia.android.config.Config;
import com.shopelia.android.model.User;

public class UserManager {

    private static final String USER_MANAGER_PRIVATE_PREFERENCE = "Shopelia$UserManager.PrivatePreference";
    private static final String PREFS_USER_JSON = "user:json";
    private static final String PREFS_AUTH_TOKEN = "user:authToken";
    private static final String PREFS_LOGINS_COUNT = "user:loginsCount";

    private static UserManager sInstance = null;

    private final Context mContext;
    private final SharedPreferences mPreferences;

    private User mUser;

    private UserManager(Context context) {
        mContext = context.getApplicationContext();
        mPreferences = context.getSharedPreferences(USER_MANAGER_PRIVATE_PREFERENCE, Context.MODE_PRIVATE);

        final String json = mPreferences.getString(PREFS_USER_JSON, null);
        if (json != null) {
            try {
                login(User.inflate(new JSONObject(json)));
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

    public void login(User user) {
        if (user == null) {
            return;
        }
        Editor editor = mPreferences.edit();
        editor.putInt(PREFS_LOGINS_COUNT, mPreferences.getInt(PREFS_LOGINS_COUNT, 0) + 1);
        editor.commit();
        mUser = user;
        saveUser();
    }

    public void saveUser() {
        try {
            Editor editor = mPreferences.edit();
            editor.putString(PREFS_USER_JSON, mUser.toJson().toString());
            editor.commit();
        } catch (JSONException e) {
            if (Config.ERROR_LOGS_ENABLED) {
                Log.e("Shopelia", "Impossible to save user", e);
            }
        }
    }

    public int getLoginsCount() {
        return mPreferences.getInt(PREFS_LOGINS_COUNT, 0);
    }

    public void setAuthToken(String token) {
        Editor editor = mPreferences.edit();
        editor.putString(PREFS_AUTH_TOKEN, token);
        editor.commit();
    }

    public String getAuthToken() {
        return mPreferences.getString(PREFS_AUTH_TOKEN, null);
    }

    public void logout() {
        mUser = null;
        mPreferences.edit().remove(PREFS_USER_JSON).commit();
        mPreferences.edit().remove(PREFS_AUTH_TOKEN).commit();
    }

    public boolean isLogged() {
        return getAuthToken() != null;
    }

    public User getUser() {
        return mUser;
    }

}
