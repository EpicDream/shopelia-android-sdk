package com.shopelia.android.remote.api;

import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.model.Address;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;

public class CommandHandler {

    public interface Callback {
        public void onAccountCreationSucceed(User user, Address address);

        public void onPaymentInformationSent(PaymentCard paymentInformation);

        public void onError(int step, JSONObject response, Exception e);

        public void onOrderConfirmation(boolean succeed);

        public void onUserRetrieved(User user);

        public void onUserDestroyed(long userId);

    }

    public static class CallbackAdapter implements Callback {

        @Override
        public void onAccountCreationSucceed(User user, Address address) {

        }

        @Override
        public void onPaymentInformationSent(PaymentCard paymentInformation) {

        }

        @Override
        public void onError(int step, JSONObject response, Exception e) {

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

    }

    public static final int STEP_DEAD = 0;
    public static final int STEP_ACCOUNT_CREATION = 1;
    public static final int STEP_SEND_PAYMENT_INFORMATION = 2;
    public static final int STEP_ORDER = 3;
    public static final int STEP_ORDERING = 4;
    public static final int STEP_CONFIRM = 5;
    public static final int STEP_WAITING_CONFIRMATION = 6;
    public static final int STEP_RETRIEVE_USER = 7;

    public static final int STATE_RUNNING = 0;
    public static final int STATE_PAUSED = 1;
    public static final int STATE_RECYCLED = 2;

    private Context mContext;
    private Callback mCallback;

    private int mCurrentStep = STEP_DEAD;
    private int mInternalState = STATE_RUNNING;

    public CommandHandler(Context context, Callback callback) {
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

    protected void fireError(int step, JSONObject response, Exception e) {
        if (mCallback != null) {
            mCallback.onError(step, response, e);
        }
    }

}
