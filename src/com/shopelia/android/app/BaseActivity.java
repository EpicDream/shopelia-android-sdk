package com.shopelia.android.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.shopelia.android.config.Config;
import com.shopelia.android.model.Order;

public class BaseActivity extends FragmentActivity {

    public static final String EXTRA_ORDER = Config.EXTRA_PREFIX + "ORDER";
    public static final String EXTRA_INIT_ORDER = Config.EXTRA_PREFIX + "INIT_ORDER";
    public static final String EXTRA_USER = Config.EXTRA_PREFIX + "USER";

    private Order mOrder;

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);

        recoverOrder(saveState == null ? getIntent().getExtras() : saveState);

        if (mOrder == null) {
            throw new UnsupportedOperationException("Activity should hold an order at this point");
        }

    }

    private void recoverOrder(Bundle bundle) {
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_INIT_ORDER)) {
                mOrder = new Order();
            } else if (bundle.containsKey(EXTRA_ORDER)) {
                mOrder = bundle.getParcelable(EXTRA_ORDER);
            }
        }
    }

    public Order getOrder() {
        return mOrder;
    }

}
