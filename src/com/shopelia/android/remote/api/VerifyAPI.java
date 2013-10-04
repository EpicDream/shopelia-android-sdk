package com.shopelia.android.remote.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.shopelia.android.R;
import com.shopelia.android.concurent.ScheduledTask;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.UserAPI.OnSignInEvent;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class VerifyAPI extends ApiController {

    public static class OnVerifySucceedEvent extends Event {

    }

    public static class OnVerifyFailedEvent extends Event {

    }

    public static class OnUpdateUiEvent extends Event {
        public final VerifyAPI api;
        public final boolean shouldBlock;
        public final long delay;
        public final String message;

        private OnUpdateUiEvent(VerifyAPI api, boolean shouldBlock, long delay, String message) {
            this.api = api;
            this.shouldBlock = shouldBlock;
            this.delay = delay;
            this.message = message;
        }

    }

    private static final Class<?>[] sEventTypes = new Class<?>[] {
            OnVerifyFailedEvent.class, OnVerifySucceedEvent.class, OnUpdateUiEvent.class
    };

    public static final String PREFERENCE_NAME = "ShopeliaVerifyAPI";

    static final String PREF_TIME_TO_BLOCK = "SPH_TIME_TO_BLOCK";
    static final String JSON_DELAY = "delay";
    private static final long REFRESH_PERIOD = 1000L;
    private static final long SECONDS = 1000L;

    private long mTimeToBlock;
    private ScheduledTask mRefreshTask = new ScheduledTask();
    private UserAPI mApi;

    public VerifyAPI(Context context) {
        super(context);
        mApi = new UserAPI(context);
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

    @Override
    public Class<?>[] getEventTypes() {
        return sEventTypes;
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
                if (httpResponse.getStatus() == 200) {
                    try {
                        Log.d(null, "VERIFY " + new JSONObject(httpResponse.getBodyAsString()).toString(2));
                        User user = User.inflate(new JSONObject(httpResponse.getBodyAsString()).getJSONObject(User.Api.USER));

                        UserManager.get(getContext()).update(user);
                    } catch (JSONException e) {
                        // Do nothing
                        e.printStackTrace();
                    }
                    getEventBus().post(new OnVerifySucceedEvent());
                } else if (httpResponse.getStatus() == 401 && object.has(User.Api.PASSWORD)
                        && UserManager.get(getContext()).getUser() != null) {
                    User user = UserManager.get(getContext()).getUser();
                    user.password = object.optString(User.Api.PASSWORD);
                    mApi.register(new Object() {

                        @SuppressWarnings("unused")
                        public void onEvent(OnSignInEvent event) {
                            getEventBus().post(new OnVerifySucceedEvent());
                            mApi.unregister(this);
                        }

                        @SuppressWarnings("unused")
                        public void onEvent(OnApiErrorEvent event) {
                            fireError(event.response, event.json, event.exception);
                            mApi.unregister(this);
                        }

                    });
                    mApi.signIn(user);
                } else {
                    if (httpResponse.getStatus() == 503) {
                        try {
                            JSONObject object = new JSONObject(httpResponse.getBodyAsString());
                            forbidOrder(object.optLong(JSON_DELAY, 300) * SECONDS);
                            startRefreshing();
                        } catch (JSONException e) {

                        }
                    } else {
                        getEventBus().post(new OnVerifyFailedEvent());
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                fireError(null, null, e);
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
            getEventBus().post(new OnUpdateUiEvent(VerifyAPI.this, true, delay, errorMessage));
            if (delay <= 0) {
                getEventBus().post(new OnUpdateUiEvent(VerifyAPI.this, false, 0, null));
                mRefreshTask.stop();
            }
        }
    };

}
