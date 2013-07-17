package com.shopelia.android.accounts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ShopeliaAccountAuthentificationService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return new ShopeliaAccountAuthenticator(this).getIBinder();
    }

}
