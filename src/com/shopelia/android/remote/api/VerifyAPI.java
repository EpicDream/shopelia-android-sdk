package com.shopelia.android.remote.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.shopelia.android.R;
import com.shopelia.android.concurent.ScheduledTask;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class VerifyAPI extends ApiHandler {

    public static final String PREFERENCE_NAME = "ShopeliaVerifyAPI";

    static final String PREF_TIME_TO_BLOCK = "SPH_TIME_TO_BLOCK";
    static final String JSON_DELAY = "delay";
    private static final long REFRESH_PERIOD = 1000L;
    private static final long SECONDS = 1000L;

    private long mTimeToBlock;
    private ScheduledTask mRefreshTask = new ScheduledTask();

    public VerifyAPI(Context context, Callback callback) {
        super(context, callback);
        mTimeToBlock = getSharedPreferences().getLong(PREF_TIME_TO_BLOCK, 0L);
        startRefreshing();
    }

    public long getUnlockDate() {
        return mTimeToBlock;
    }

    public long getUnlockDelay() {
        return getUnlockDate() - System.currentTimeMillis();
    }

    public boolean isOrderForbidden() {
        return getUnlockDelay() > 0;
    }

    private void forbidOrder(long delay) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        mTimeToBlock = System.currentTimeMillis() + delay;
        editor.putLong(PREF_TIME_TO_BLOCK, mTimeToBlock);
        editor.commit();
    }

    public boolean verify(JSONObject object) {
        if (isOrderForbidden()) {
            return false;
        }
        ShopeliaRestClient.V1(getContext()).post(Command.V1.Users.Verify.$, object, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {
                if (httpResponse.getStatus() == 204 && hasCallback()) {
                    getCallback().onVerifySucceed();
                } else if (hasCallback()) {
                    if (httpResponse.getStatus() == 503) {
                        try {
                            JSONObject object = new JSONObject(httpResponse.getBodyAsString());
                            forbidOrder(object.optLong(JSON_DELAY, 300) * SECONDS);
                            startRefreshing();
                        } catch (JSONException e) {

                        }
                    } else {
                        getCallback().onVerifyFailed();
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                if (hasCallback()) {
                    getCallback().onError(STEP_VERIFY, null, null, e);
                }
            }

        });
        return true;
    }

    private void startRefreshing() {
        if (isOrderForbidden()) {
            mRefreshTask.scheduleAtFixedRate(mRefreshUiRunnable, getUnlockDelay() % 1000, REFRESH_PERIOD);
            mRefreshUiRunnable.run();
        }
    }

    private SharedPreferences getSharedPreferences() {
        return getContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private Resources getResources() {
        return getContext().getResources();
    }

    private Runnable mRefreshUiRunnable = new Runnable() {

        @Override
        public void run() {
            String errorMessage;
            long delay = getUnlockDelay();
            if (delay >= 60 * 60 * 1000) {
                errorMessage = getResources().getString(R.string.shopelia_pincode_retry,
                        getResources().getQuantityString(R.plurals.hour, (int) delay / (60 * 60 * 1000), delay / (60 * 60 * 1000)));
            } else if (delay >= 60 * 1000) {
                errorMessage = getResources().getString(
                        R.string.shopelia_pincode_retry,
                        getResources().getQuantityString(R.plurals.minute, (int) delay / (60 * 1000), delay / (60 * 1000)),
                        getResources().getQuantityString(R.plurals.second, (int) (delay % (60 * 1000)) / (1000),
                                (delay % (60 * 1000)) / (1000)));
            } else if (delay >= 1000) {
                errorMessage = getResources().getString(R.string.shopelia_pincode_retry_seconds,
                        getResources().getQuantityString(R.plurals.second, (int) delay / (1000), delay / (1000)));
            } else {
                errorMessage = null;
            }
            if (hasCallback()) {
                getCallback().onVerifyUpdateUI(VerifyAPI.this, true, delay, errorMessage);
            }
            if (delay <= 0) {
                getCallback().onVerifyUpdateUI(VerifyAPI.this, false, 0, null);
                mRefreshTask.stop();
            }
        }
    };

}
