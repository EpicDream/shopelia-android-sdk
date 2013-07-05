package com.shopelia.android.app;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.shopelia.android.R;
import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.Order;
import com.shopelia.android.remote.api.ShopeliaHttpSynchronizer;
import com.shopelia.android.utils.DigestUtils;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.ActionBar.OnItemClickListener;
import com.shopelia.android.widget.actionbar.ActionBarWidget;
import com.shopelia.android.widget.actionbar.ProgressBarItem;

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

    public static final String EXTRA_SESSION_ID = Config.EXTRA_PREFIX + "PRIVATE_SESSION_ID";

    public static final String EXTRA_STYLE = Config.EXTRA_PREFIX + "STYLE";

    public static final int REQUEST_CHECKOUT = 0x1602;
    public static final int RESULT_FAILURE = 0xfa15e;
    public static final int RESULT_LOGOUT = 0xd04e;

    public static final int STYLE_FULLSCREEN = 0x0;
    public static final int STYLE_DIALOG = 0x1;
    public static final int STYLE_TRANSLUCENT = STYLE_FULLSCREEN;

    public static final int MODE_CLEARED = 0x0;
    public static final int MODE_WAITING = 1 << 0;
    public static final int MODE_BLOCKED = 1 << 1;

    private Order mOrder;
    private FrameLayout mRootView;
    private ActionBar mActionBar;
    private Handler mHandler = new Handler();
    private int mMode = MODE_CLEARED;
    @SuppressWarnings("rawtypes")
    private List<WeakReference<ShopeliaFragment>> mAttachedFragment = new ArrayList<WeakReference<ShopeliaFragment>>();
    private ProgressDialog mProgressDialog;

    private String mSessionId;
    private ShopeliaTracker mTrackingObject = ShopeliaTracker.Factory.create(ShopeliaTracker.MIXPANEL);

    private Runnable mWaitModeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mActionBar = new ActionBar(this);
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSessionId = savedInstanceState.getString(EXTRA_SESSION_ID);
        } else {
            mSessionId = getIntent().getExtras().getString(EXTRA_SESSION_ID);
        }

        if (isTracked()) {
            mTrackingObject.init(this);
            if (savedInstanceState == null) {
                fireScreenSeenEvent(getActivityName());
            }
        }

        setContentView(getActivityStyle() == STYLE_FULLSCREEN ? R.layout.shopelia_host_activity : R.layout.shopelia_host_activity_dialog);
        mRootView = (FrameLayout) super.findViewById(R.id.host_container);
        mActionBar.bindWidget((ActionBarWidget) super.findViewById(R.id.action_bar));
        mActionBar.setOnItemClickListener(mOnActionBarItemClickListener);
        if (isPartOfOrderWorkFlow()) {
            recoverOrder(savedInstanceState == null ? getIntent().getExtras() : savedInstanceState);
            if (mOrder == null) {
                throw new UnsupportedOperationException("Activity should hold an order at this point");
            }
        }

        if (savedInstanceState == null) {

        }

        if (getActivityStyle() == STYLE_DIALOG) {
            super.findViewById(R.id.frame).setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            super.findViewById(R.id.outside_area).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

    }

    protected void createSessionId(long value, String str) {
        String input = value + str;
        mSessionId = DigestUtils.SHA1(input, "UTF-8");
    }

    public void fireScreenSeenEvent(String screenName) {
        if (screenName == null) {
            return;
        }
        JSONObject properties = new JSONObject();
        try {
            properties.put(Analytics.Properties.SCREEN_NAME, screenName);
        } catch (JSONException e) {

        }
        track(Analytics.Events.Activities.SCREEN_SEEN, properties);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ShopeliaHttpSynchronizer.flush(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTrackingObject.flush();
        ShopeliaHttpSynchronizer.flush(this);
    }

    public void track(String eventName, JSONObject properties) {
        mTrackingObject.track(eventName, properties);
    }

    public void track(String eventName) {
        mTrackingObject.track(eventName);
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
        getShopeliaActionBar().save();
        if (blockUi) {
            setWaitingMode(true);
            mProgressDialog = ProgressDialog.show(this, getString(R.string.shopelia_dialog_title), message);
        } else {
            setQuietWaitingMode(true);
            getShopeliaActionBar().clear();
            getShopeliaActionBar().addItem(new ProgressBarItem(0, message.toString()));
            getShopeliaActionBar().commit();
        }
    }

    public void startDelayedWaiting(final CharSequence message, final boolean blockUi, final boolean isCancelable, long delay) {
        mWaitModeRunnable = new Runnable() {

            @Override
            public void run() {
                startWaiting(message, blockUi, isCancelable);
            }
        };
        mHandler.postDelayed(mWaitModeRunnable, delay);
    }

    /**
     * Stop the waiting mode started with
     * {@link ShopeliaActivity#startWaiting(CharSequence, boolean, boolean)}
     */
    public void stopWaiting() {
        if (mWaitModeRunnable != null) {
            mHandler.removeCallbacks(mWaitModeRunnable);
            mWaitModeRunnable = null;
        }
        if ((mMode & MODE_WAITING) == MODE_WAITING) {
            getShopeliaActionBar().restore();
            getShopeliaActionBar().commit();
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
        setWaitingMode(false);
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

    public Handler getHandler() {
        return mHandler;
    }

    public void post(Runnable runnable) {
        mHandler.post(runnable);
    }

    public void postDelayed(Runnable runnable, long delay) {
        mHandler.postDelayed(runnable, delay);
    }

    @Override
    public void startActivity(Intent intent) {
        if (intent != null) {
            intent.putExtra(EXTRA_SESSION_ID, getSessionId());
        }
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (intent != null) {
            intent.putExtra(EXTRA_SESSION_ID, getSessionId());
        }
        super.startActivityForResult(intent, requestCode);
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
        outState.putString(EXTRA_SESSION_ID, getSessionId());
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

    public ActionBar getShopeliaActionBar() {
        return mActionBar;
    }

    public void setActivityStyle(int style) {
        getIntent().putExtra(EXTRA_STYLE, style);
    }

    public int getActivityStyle() {
        return getIntent().getIntExtra(EXTRA_STYLE, STYLE_FULLSCREEN);
    }

    public void closeSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
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

    public String getSessionId() {
        return mSessionId;
    }

}
