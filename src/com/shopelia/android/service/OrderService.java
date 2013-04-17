package com.shopelia.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class OrderService extends Service {

    public class OrderBinder extends Binder {

        public OrderService getService() {
            return OrderService.this;
        }

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(null, "ON START COMMAND");
        return START_STICKY;
    }
}
