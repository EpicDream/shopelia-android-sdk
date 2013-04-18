package com.shopelia.android.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.shopelia.android.R;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.Order;

/**
 * Base activity of the shopelia SDK. This Activity has a default appearance and
 * is a fragment container.
 * 
 * @author Pierre Pollastri
 */
public class HostActivity extends FragmentActivity {

    public static final String EXTRA_ORDER = Config.EXTRA_PREFIX + "ORDER";
    protected static final String EXTRA_INIT_ORDER = Config.EXTRA_PREFIX + "INIT_ORDER";
    public static final String EXTRA_USER = Config.EXTRA_PREFIX + "USER";

    private Order mOrder;
    private FrameLayout mRootView;

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);

        setContentView(R.layout.host_activity);
        mRootView = (FrameLayout) super.findViewById(R.id.host_container);
        recoverOrder(saveState == null ? getIntent().getExtras() : saveState);

        if (mOrder == null) {
            throw new UnsupportedOperationException("Activity should hold an order at this point");
        }

    }

    protected void setHostContentView(int resId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        setHostContentView(inflater.inflate(resId, null));
    }

    protected void setHostContentView(View rootView) {
        mRootView.removeAllViews();
        mRootView.addView(rootView);
    }

    @Override
    public View findViewById(int id) {
        return mRootView.findViewById(id);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_ORDER, mOrder);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mOrder = savedInstanceState.getParcelable(EXTRA_ORDER);
    }

    public Order getOrder() {
        return mOrder;
    }

}
