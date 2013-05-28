package com.shopelia.android.app;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shopelia.android.R;
import com.shopelia.android.api.ShopeliaActivityPath;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.Order;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.ActionBar.OnItemClickListener;
import com.shopelia.android.widget.actionbar.ActionBarWidget;

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
    public static final int RESULT_LOGOUT = 0xd04e;

    public static final int MODE_CLEARED = 0x0;
    public static final int MODE_WAITING = 1 << 0;
    public static final int MODE_BLOCKED = 1 << 1;

    private static final String MIXPANEL_API_TOKEN = "95c15bf80bf7bf93cb1c673865c75a22";

    private Order mOrder;
    private ShopeliaActivityPath mCurrentActivity;
    private FrameLayout mRootView;
    private ActionBar mActionBar;
    private int mMode = MODE_CLEARED;
    @SuppressWarnings("rawtypes")
    private List<WeakReference<ShopeliaFragment>> mAttachedFragment = new ArrayList<WeakReference<ShopeliaFragment>>();

    private ProgressDialog mProgressDialog;

    private MixpanelAPI mMixpanelInstance;

    @Override
    protected void onCreate(Bundle saveState) {
        mActionBar = new ActionBar(this);
        super.onCreate(saveState);

        if (isTracked()) {
            mMixpanelInstance = MixpanelAPI.getInstance(this, MIXPANEL_API_TOKEN);
        }

        setContentView(R.layout.shopelia_host_activity);
        mRootView = (FrameLayout) super.findViewById(R.id.host_container);
        mActionBar.bindWidget((ActionBarWidget) super.findViewById(R.id.action_bar));
        mActionBar.setOnItemClickListener(mOnActionBarItemClickListener);
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTracked()) {
            mMixpanelInstance.flush();
        }
    }

    public void track(String eventName, JSONObject properties) {
        if (isTracked()) {
            mMixpanelInstance.track(eventName, properties);
        }
    }

    /**
     * Easy way to set the current {@link ShopeliaActivity} in waiting mode
     * (same as ShopeliaActivity#setHostMode(value ? getMode() | MODE_BLOCKED |
     * MODE_WAITING) : getMode() & ~(MODE_BLOCKED | MODE_WAITING)). Note that
     * this method will block ui events.
     * 
     * @param value
     */
    public void setWaitingMode(boolean value) {
        setHostMode(value ? getMode() | MODE_BLOCKED | MODE_WAITING : getMode() & ~(MODE_BLOCKED | MODE_WAITING));
    }

    /**
     * Initialize the waiting mode of the activity.
     * 
     * @param message The message to explain why you force the user to wait
     * @param blockUi Block events on the activity if true
     * @param isCancelable Action is cancelable (true or false)
     */
    public void startWaiting(CharSequence message, boolean blockUi, boolean isCancelable) {
        setWaitingMode(true);
        if (blockUi) {
            mProgressDialog = ProgressDialog.show(this, getString(R.string.shopelia_dialog_title), message);
        }
    }

    /**
     * Stop the waiting mode started with
     * {@link ShopeliaActivity#startWaiting(CharSequence, boolean, boolean)}
     */
    public void stopWaiting() {
        setWaitingMode(false);
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * Easy to set the current {@link ShopeliaActivity} in waiting mode.
     * 
     * @param value
     */
    public void setQuietWaitingMode(boolean value) {
        if (value) {
            setHostMode(getMode() | MODE_WAITING);
        } else {
            setHostContentView(getMode() & ~(MODE_WAITING));
        }
    }

    /**
     * Set special modes for the activity (like
     * {@link ShopeliaActivity#MODE_WAITING})
     * 
     * @param mode
     */
    public void setHostMode(int mode) {
        mMode = mode;
        invalidate();
    }

    /**
     * Force the activity to invalidate its mode
     */
    public void invalidate() {
        if ((mMode & MODE_WAITING) == MODE_WAITING) {

        } else {

        }
        if ((mMode & MODE_BLOCKED) == MODE_BLOCKED) {
            mRootView.setEnabled(false);
        } else {
            mRootView.setEnabled(true);
        }
    }

    /**
     * Returns the current activity mode
     * 
     * @return
     */
    public int getMode() {
        return mMode;
    }

    public MixpanelAPI getTrackingApi() {
        return mMixpanelInstance;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onAttachFragment(android.support.v4.app.Fragment fragment) {
        super.onAttachFragment(fragment);
        Iterator<WeakReference<ShopeliaFragment>> it = mAttachedFragment.iterator();
        while (it.hasNext()) {
            if (it.next().get() == null) {
                it.remove();
            }
        }
        if (fragment instanceof ShopeliaFragment) {
            mAttachedFragment.add(new WeakReference<ShopeliaFragment>((ShopeliaFragment) fragment));
        }
        if (fragment instanceof ShopeliaFragment<?>) {
            ((ShopeliaFragment<?>) fragment).onAttach();
        }
    }

    protected void onCreateShopeliaActionBar(ActionBar actionBar) {

    }

    @SuppressWarnings("rawtypes")
    protected void onActionItemSelected(Item item) {
        for (WeakReference<ShopeliaFragment> fragment : mAttachedFragment) {
            if (fragment.get() != null && !fragment.get().isDetached()) {
                fragment.get().onActionItemSelected(item);
            }
        }
    }

    /**
     * Inflates the content view of the activity into its root view (with action
     * bar and Shopelia UI kit)
     * 
     * @param resId
     */
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

    public void setOrder(Order order) {
        mOrder = order;
    }

    public void stopRecording() {
        mCurrentActivity.stopRecording();
    }

    public ActionBar getShopeliaActionBar() {
        return mActionBar;
    }

    protected boolean isPartOfOrderWorkFlow() {
        return true;
    }

    protected boolean isTracked() {
        return true;
    }

    public abstract String getActivityName();

    private OnItemClickListener mOnActionBarItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(Item item) {
            onActionItemSelected(item);
        }
    };

}
