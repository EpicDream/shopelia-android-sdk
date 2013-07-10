package com.shopelia.android.remote.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.shopelia.android.R;
import com.shopelia.android.concurent.ScheduledTask;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
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

    public boolean verify(final JSONObject object) {
        if (isOrderForbidden()) {
            return false;
        }
        ShopeliaRestClient.V2(getContext()).post(Command.V1.Users.Verify.$, object, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {
                if (httpResponse.getStatus() == 200 && hasCallback()) {
                    try {
                        User user = User.inflate(new JSONObject(httpResponse.getBodyAsString()).getJSONObject(User.Api.USER));
                        UserManager.get(getContext()).update(user);
                    } catch (JSONException e) {
                        // Do nothing
                    }
                    getCallback().onVerifySucceed();
                } else if (hasCallback() && httpResponse.getStatus() == 401 && object.has(User.Api.PASSWORD)
                        && UserManager.get(getContext()).getUser() != null) {
                    User user = UserManager.get(getContext()).getUser();
                    user.password = object.optString(User.Api.PASSWORD);
                    new UserAPI(getContext(), new CallbackAdapter() {
                        @Override
                        public void onSignIn(User user) {
                            super.onSignIn(user);
                            getCallback().onVerifySucceed();
                        }

                        @Override
                        public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
                            super.onError(step, httpResponse, response, e);
                            if (hasCallback()) {
                                getCallback().onError(STEP_VERIFY, httpResponse, response, e);
                            }
                        }

                    }).signIn(user);
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
