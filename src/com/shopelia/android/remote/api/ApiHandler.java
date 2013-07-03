package com.shopelia.android.remote.api;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.config.Config;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.Merchant;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.turbomanage.httpclient.HttpResponse;

public class ApiHandler {

    public interface Callback {
        public void onAccountCreationSucceed(User user, Address address);

        public void onPaymentCardAdded(PaymentCard paymentCard);

        public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e);

        public void onOrderConfirmation(boolean succeed);

        public void onUserRetrieved(User user);

        public void onUserUpdateDone();

        public void onUserDestroyed(long userId);

        public void onSignIn(User user);

        public void onRetrieveMerchant(Merchant merchant);

        public void onRetrieveMerchants(ArrayList<Merchant> merchants);

        public void onSignOut();

        public void onAddressAdded(Address address);

        public void onAddressEdited(Address address);

        public void onAddressDeleted(long id);

    }

    public static class CallbackAdapter implements Callback {

        @Override
        public void onAccountCreationSucceed(User user, Address address) {

        }

        @Override
        public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {

        }

        @Override
        public void onOrderConfirmation(boolean succeed) {

        }

        @Override
        public void onUserRetrieved(User user) {

        }

        @Override
        public void onUserDestroyed(long userId) {

        }

        @Override
        public void onSignIn(User user) {

        }

        @Override
        public void onRetrieveMerchant(Merchant merchant) {

        }

        @Override
        public void onRetrieveMerchants(ArrayList<Merchant> merchants) {

        }

        @Override
        public void onSignOut() {

        }

        @Override
        public void onAddressAdded(Address address) {

        }

        @Override
        public void onAddressEdited(Address address) {

        }

        @Override
        public void onAddressDeleted(long id) {

        }

        @Override
        public void onUserUpdateDone() {

        }

        @Override
        public void onPaymentCardAdded(PaymentCard paymentCard) {

        }

    }

    public static class ErrorInflater {

        public interface Api {
            String ERROR = "error";
        }

        public static JSONObject inflate(String source) {
            try {
                return new JSONObject(source);
            } catch (JSONException e) {
                JSONObject object = new JSONObject();
                try {
                    object.put(Api.ERROR, source);
                } catch (JSONException e1) {

                }
                return object;
            }
        }

    }

    public static final int STEP_DEAD = 0;
    public static final int STEP_ACCOUNT_CREATION = 1;
    public static final int STEP_SEND_PAYMENT_INFORMATION = 2;
    public static final int STEP_ORDER = 3;
    public static final int STEP_ORDERING = 4;
    public static final int STEP_CONFIRM = 5;
    public static final int STEP_WAITING_CONFIRMATION = 6;
    public static final int STEP_RETRIEVE_USER = 7;
    public static final int STEP_SIGN_IN = 8;
    public static final int STEP_ADDRESS = 9;

    public static final int STATE_RUNNING = 0;
    public static final int STATE_PAUSED = 1;
    public static final int STATE_RECYCLED = 2;

    private Context mContext;
    private Callback mCallback;

    private int mCurrentStep = STEP_DEAD;
    private int mInternalState = STATE_RUNNING;

    public ApiHandler(Context context, Callback callback) {
        this.mContext = context;
        setCallback(callback);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public boolean hasCallback() {
        return mCallback != null;
    }

    public Callback getCallback() {
        return mCallback;
    }

    public Context getContext() {
        return mContext;
    }

    public int getCurrentStep() {
        return mCurrentStep;
    }

    public int getInternalState() {
        return mInternalState;
    }

    protected void setCurrentStep(int step) {
        mCurrentStep = step;
    }

    protected void setInternalState(int state) {
        mInternalState = state;
    }

    public void stopOrderForError() {

    }

    public void pause() {
        mInternalState = STATE_PAUSED;
    }

    public void resume() {
        mInternalState = STATE_RUNNING;
    }

    public void recycle() {
        mInternalState = STATE_RECYCLED;
    }

    protected void fireError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
        if (Config.INFO_LOGS_ENABLED && e != null) {
            e.printStackTrace();
        }
        if (mCallback != null) {
            mCallback.onError(step, httpResponse, response, e);
        }
    }

}
