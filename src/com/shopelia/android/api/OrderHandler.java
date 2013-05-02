package com.shopelia.android.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.shopelia.android.http.HttpPoller;
import com.shopelia.android.http.HttpPoller.PollerCalback;
import com.shopelia.android.http.JsonAsyncCallback;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.OrderState;
import com.shopelia.android.model.OrderState.State;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public final class OrderHandler {

    public interface Callback {
        public void onAccountCreationSucceed(User user, Address address);

        public void onPaymentInformationSent(PaymentCard paymentInformation);

        public void onOrderBegin(Order order);

        public void onOrderStateUpdate(OrderState newState);

        public void onError(int step, JSONObject response, Exception e);

        public void onOrderConfirmation(boolean succeed);

    }

    public static final int STEP_ACCOUNT_CREATION = 1;
    public static final int STEP_SEND_PAYMENT_INFORMATION = 2;
    public static final int STEP_ORDER = 3;
    public static final int STEP_ORDERING = 4;
    public static final int STEP_CONFIRM = 5;

    private Context mContext;
    private Callback mCallback;

    private static HttpPoller sHttpPoller;

    private PaymentCard mPaymentCard;
    private OrderState mOrderState;

    public OrderHandler(Context context, Callback callback) {
        this.mContext = context;
        setCallback(callback);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
        if (mCallback == null) {
            throw new NullPointerException("Callback cannot be null");
        }
    }

    public void createAccount(final User user, final Address address) {

        if (true) {
            mCallback.onAccountCreationSucceed(user, address);
            return;
        }

        JSONObject params = new JSONObject();

        try {
            params.put(Order.Api.USER, User.createObjectForAccountCreation(user, address));

        } catch (JSONException e) {
            fireError(STEP_ACCOUNT_CREATION, null, e);
            return;
        }

        ShopeliaRestClient.post(Command.V1.Users.$, params, new JsonAsyncCallback() {

            @Override
            public void onComplete(HttpResponse response, JSONObject object) {
                if (mCallback != null && object.has(User.Api.USER) && object.has(User.Api.AUTH_TOKEN)) {
                    UserManager.get(mContext).login(user);
                    UserManager.get(mContext).setAuthToken(object.optString(User.Api.AUTH_TOKEN));
                    UserManager.get(mContext).saveUser();
                    mCallback.onAccountCreationSucceed(user, address);
                } else {
                    fireError(STEP_ACCOUNT_CREATION, object, null);
                }
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                fireError(STEP_ACCOUNT_CREATION, null, e);
            }

        });

    }

    public void sendPaymentInformation(User user, PaymentCard card) {
        JSONObject params = new JSONObject();

        try {
            JSONObject cardObject = card.toJson();
            cardObject.put(PaymentCard.Api.NAME, user.lastName);
            params.put(PaymentCard.Api.PAYMENT_CARD, cardObject);

        } catch (JSONException e) {
            fireError(STEP_SEND_PAYMENT_INFORMATION, null, e);
            return;
        }
        Log.d(null, "SEND " + params);
        ShopeliaRestClient.authenticate(mContext);
        ShopeliaRestClient.post(Command.V1.PaymentCards.$, params, new JsonAsyncCallback() {

            @Override
            public void onComplete(HttpResponse response, JSONObject object) {
                try {
                    PaymentCard card = PaymentCard.inflate(object.getJSONObject(PaymentCard.Api.PAYMENT_CARD));
                    if (mCallback != null) {
                        mPaymentCard = card;
                        mCallback.onPaymentInformationSent(card);
                    }
                } catch (JSONException e) {
                    fireError(STEP_SEND_PAYMENT_INFORMATION, null, e);
                }
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                fireError(STEP_SEND_PAYMENT_INFORMATION, null, e);
            }

        });

    }

    public void order(final Order order) {
        JSONObject params = new JSONObject();
        try {
            JSONObject orderObject = new JSONObject();
            JSONArray urls = new JSONArray();
            urls.put(order.productUrl);
            orderObject.put(Order.Api.PRODUCT_URLS, urls);
            params.put(Order.Api.ORDER, orderObject);
        } catch (JSONException e) {
            fireError(STEP_ORDER, null, e);
            return;
        }
        ShopeliaRestClient.authenticate(mContext);
        ShopeliaRestClient.post(Command.V1.Orders.$, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {

                if (httpResponse.getStatus() == 201) {
                    try {
                        JSONObject object = new JSONObject(httpResponse.getBodyAsString());
                        order.uuid = object.getJSONObject(Order.Api.ORDER).getString(Order.Api.UUID);
                        if (mCallback != null) {
                            mCallback.onOrderBegin(order);
                        }
                        OrderState state = OrderState.inflate(object.getJSONObject(OrderState.Api.ORDER));
                        mOrderState = state;
                        if (mCallback != null) {
                            mCallback.onOrderStateUpdate(state);
                        }

                        if (sHttpPoller == null) {
                            sHttpPoller = new HttpPoller();
                            sHttpPoller.start();
                        }

                        if (!sHttpPoller.isStopped()) {
                            sHttpPoller.end();
                        }

                        sHttpPoller.setPollerCallback(mPollerCalback);
                        sHttpPoller.poll(Command.V1.Orders.Order(order.uuid));

                    } catch (JSONException e) {
                        fireError(STEP_ORDER, null, e);
                    }
                } else {
                    fireError(STEP_ORDER, null, new IllegalStateException(httpResponse.getBodyAsString()));
                }

            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                fireError(STEP_ORDER, null, e);
            }

        });
    }

    public boolean confirm() {
        if (canConfirm()) {
            JSONObject params = new JSONObject();
            try {
                JSONObject paymentCard = new JSONObject();
                paymentCard.put(PaymentCard.Api.PAYMENT_CARD_ID, mPaymentCard.id);
                params.put(OrderState.Api.VERB, OrderState.Verb.CONFIRM.toString());
                params.put(OrderState.Api.CONTENT, paymentCard);

                ShopeliaRestClient.put(Command.V1.Orders.Order(mOrderState.uuid), params, new AsyncCallback() {

                    @Override
                    public void onComplete(HttpResponse httpResponse) {
                        if (httpResponse.getStatus() == 200) {

                        } else {

                        }
                    }
                });

                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void cancel() {
        if (mOrderState == null) {
            return;
        }
        JSONObject params = new JSONObject();
        try {
            params.put(OrderState.Api.VERB, OrderState.Verb.CANCEL.toString());

            ShopeliaRestClient.put(Command.V1.Orders.Order(mOrderState.uuid), params, new AsyncCallback() {

                @Override
                public void onComplete(HttpResponse httpResponse) {

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean canConfirm() {
        return mOrderState != null && mOrderState.state == State.PENDING_CONFIRMATION && mPaymentCard != null
                && mPaymentCard.id != PaymentCard.INVALID_ID;
    }

    public void stopOrderForError() {
        if (sHttpPoller != null) {
            sHttpPoller.end();
        }
    }

    public void pause() {
        if (sHttpPoller != null) {
            sHttpPoller.pause();
        }
    }

    public void resume() {
        if (sHttpPoller != null) {
            sHttpPoller.resumePolling();
        }
    }

    private void fireError(int step, JSONObject response, Exception e) {
        if (mCallback != null) {
            mCallback.onError(step, response, e);
        }
    }

    private HttpPoller.PollerCalback mPollerCalback = new PollerCalback() {

        @Override
        protected void onComplete(HttpResponse response) {
            if (response.getStatus() == 200) {
                try {
                    OrderState state = OrderState.inflate(new JSONObject(response.getBodyAsString()).getJSONObject(OrderState.Api.ORDER));
                    mOrderState = state;
                    if (mCallback != null) {
                        mCallback.onOrderStateUpdate(state);
                        if (canConfirm()) {
                            sHttpPoller.end();
                        }
                    }
                } catch (JSONException e) {
                    fireError(STEP_ORDERING, null, e);
                }
            } else {
                fireError(STEP_ORDERING, null, null);
            }
        }

        @Override
        protected void onError(Exception e) {
            fireError(STEP_ORDERING, null, e);
        };

    };

}
