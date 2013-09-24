package com.shopelia.android.remote.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.http.JsonAsyncCallback;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.turbomanage.httpclient.HttpResponse;

public class PaymentCardAPI extends ApiController {

    public static class OnAddPaymentCardEvent extends OnAddResourceEvent<PaymentCard> {

        protected OnAddPaymentCardEvent(PaymentCard resource) {
            super(resource);
        }

    }

    private static Class<?>[] sEventTypes = new Class<?>[] {
        OnAddPaymentCardEvent.class
    };

    public PaymentCardAPI(Context context) {
        super(context);
    }

    public void addPaymentCard(PaymentCard card) {
        final User user = UserManager.get(getContext()).getUser();
        JSONObject params = new JSONObject();
        try {
            JSONObject cardObject = card.toJson();
            cardObject.put(PaymentCard.Api.NAME, user.lastname);
            params.put(PaymentCard.Api.PAYMENT_CARD, cardObject);
            params = cardObject;
        } catch (JSONException e) {
            fireError(null, null, e);
            return;
        }
        ShopeliaRestClient.V1(getContext()).post(Command.V1.PaymentCards.$, params, new JsonAsyncCallback() {

            @Override
            public void onComplete(HttpResponse response, JSONObject object) {
                try {
                    PaymentCard card = PaymentCard.inflate(object.getJSONObject(PaymentCard.Api.PAYMENT_CARD));
                    user.paymentCards.add(card);
                    getEventBus().post(new OnAddPaymentCardEvent(card));
                } catch (JSONException e) {
                    fireError(response, null, e);
                }
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                fireError(null, null, e);
            }

        });

    }

    @Override
    public Class<?>[] getEventTypes() {
        return sEventTypes;
    }

}
