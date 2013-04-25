package com.shopelia.android.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public final class OrderHandler {

    public interface Callback {
        public void onAccountCreationSucceed(User user, Address address);

        public void onPaymentInformationSent(PaymentCard paymentInformation);

        // public void onOrderStateUpdate();

        public void onError(int step, JSONObject response, Exception e);

    }

    public static final int STEP_ACCOUNT_CREATION = 1;
    public static final int STEP_SEND_PAYMENT_INFORMATION = 2;
    public static final int STEP_ORDER = 3;
    public static final int STEP_CONFIRM = 4;

    private Context mContext;
    private Callback mCallback;

    public OrderHandler(Context context, Callback callback) {
        this.mContext = context;
    }

    public void createAccount(User user, Address address) {
        JSONObject params = new JSONObject();

        try {
            params.put(Order.Api.USER, User.createObjectForAccountCreation(user, address));
        } catch (JSONException e) {
            fireError(STEP_ACCOUNT_CREATION, null, e);
            return;
        }

        ShopeliaRestClient.post(Command.V1.Users.$, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {
                Log.d(null, "GOT " + httpResponse.getBodyAsString());
            }
        });

    }

    public void sendPaymentInformation(PaymentCard card) {

    }

    public void order(Order order) {

    }

    public void confirm() {

    }

    private void fireError(int step, JSONObject response, Exception e) {
        if (mCallback != null) {
            mCallback.onError(step, response, e);
        }
    }

    private AsyncCallback mOnCreateAccount = new AsyncCallback() {

        @Override
        public void onComplete(HttpResponse httpResponse) {

        }
    };

    private AsyncCallback mOnSendPaymentInformation = new AsyncCallback() {

        @Override
        public void onComplete(HttpResponse httpResponse) {

        }
    };

    private AsyncCallback mOnOrder = new AsyncCallback() {

        @Override
        public void onComplete(HttpResponse httpResponse) {
            // TODO Auto-generated method stub

        }
    };

    private AsyncCallback mOnConfirm = new AsyncCallback() {

        @Override
        public void onComplete(HttpResponse httpResponse) {

        }
    };

}
