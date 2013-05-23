package com.shopelia.android.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.shopelia.android.R;
import com.shopelia.android.api.ShopeliaActivityPath;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.Order;

/**
 * Base activity of the shopelia SDK. This Activity has a default appearance and
 * is a fragment container.
 * 
 * @author Pierre Pollastri
 */
public abstract class ShopeliaActivity extends FragmentActivity {

    public static final String EXTRA_ORDER = Config.EXTRA_PREFIX + "ORDER";
    protected static final String EXTRA_INIT_ORDER = Config.EXTRA_PREFIX + "INIT_ORDER";
    public static final String EXTRA_USER = Config.EXTRA_PREFIX + "USER";

    public static final int REQUEST_CHECKOUT = 0x1602;
    public static final int RESULT_FAILURE = 0xfa15e;

    private Order mOrder;
    private ShopeliaActivityPath mCurrentActivity;
    private FrameLayout mRootView;

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);

        setContentView(R.layout.shopelia_host_activity);
        mRootView = (FrameLayout) super.findViewById(R.id.host_container);

        if (isPartOfOrderWorkFlow()) {
            recoverOrder(saveState == null ? getIntent().getExtras() : saveState);
            if (mOrder == null) {
                throw new UnsupportedOperationException("Activity should hold an order at this point");
            }
        }

        if (saveState == null) {
            mCurrentActivity = new ShopeliaActivityPath();
            mCurrentActivity.setActivityName(getActivityName());
            mCurrentActivity.startRecording();
        }
        super.findViewById(R.id.test_button).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

            }
        });
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
        if (isPartOfOrderWorkFlow() && mOrder == null) {
            throw new UnsupportedOperationException("Activity should hold an order at this point");
        }
    }

    public Order getOrder() {
        if (mOrder == null) {
            if (isPartOfOrderWorkFlow()) {
                recoverOrder(getIntent().getExtras());
                if (mOrder == null) {
                    throw new UnsupportedOperationException("Activity should hold an order at this point");
                }
            }
        }
        return mOrder;
    }

    public void stopRecording() {
        mCurrentActivity.stopRecording();
    }

    protected boolean isPartOfOrderWorkFlow() {
        return true;
    }

    public abstract String getActivityName();

}
