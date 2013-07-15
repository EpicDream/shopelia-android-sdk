package com.shopelia.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.shopelia.android.config.Config;
import com.shopelia.android.manager.UserManager;

public class ShopeliaService extends IntentService {

    public static final String ACTION = "com.shopelia.android.action.SHOPELIA";
    public static final String NAME = "Shopelia Service";
    public static final String EXTRA_SESSION = Config.EXTRA_PREFIX + "SESSION";

    public ShopeliaService() {
        super(NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SHOPELIA", "Package = " + getPackageName() + " + " + UserManager.get(getApplication()).getAuthToken());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("SHOPELIA", "Handle intent " + getPackageName());
    }

}
