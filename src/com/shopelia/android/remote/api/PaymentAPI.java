package com.shopelia.android.remote.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.http.JsonAsyncCallback;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.turbomanage.httpclient.HttpResponse;

public class PaymentAPI extends ApiHandler {

    public PaymentAPI(Context context, Callback callback) {
        super(context, callback);
    }

    public void addPaymentCard(PaymentCard card) {
        final User user = UserManager.get(getContext()).getUser();
        JSONObject params = new JSONObject();
        setCurrentStep(STEP_SEND_PAYMENT_INFORMATION);
        try {
            JSONObject cardObject = card.toJson();
            cardObject.put(PaymentCard.Api.NAME, user.lastname);
            params.put(PaymentCard.Api.PAYMENT_CARD, cardObject);
            params = cardObject;
        } catch (JSONException e) {
            fireError(STEP_SEND_PAYMENT_INFORMATION, null, null, e);
            return;
        }
        ShopeliaRestClient.authenticate(getContext());
        ShopeliaRestClient.post(Command.V1.PaymentCards.$, params, new JsonAsyncCallback() {

            @Override
            public void onComplete(HttpResponse response, JSONObject object) {
                try {
                    PaymentCard card = PaymentCard.inflate(object.getJSONObject(PaymentCard.Api.PAYMENT_CARD));
                    user.paymentCards.add(card);
                    if (hasCallback()) {
                        getCallback().onPaymentCardAdded(card);
                    }
                } catch (JSONException e) {
                    fireError(STEP_SEND_PAYMENT_INFORMATION, response, null, e);
                }
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                fireError(STEP_SEND_PAYMENT_INFORMATION, null, null, e);
            }

        });

    }

}
