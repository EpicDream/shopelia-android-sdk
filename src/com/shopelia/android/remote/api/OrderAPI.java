package com.shopelia.android.remote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.utils.FormatUtils;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class OrderAPI extends ApiHandler {

    public OrderAPI(Context context, Callback callback) {
        super(context, callback);
    }

    public void order(final Order order) {

        JSONObject params = new JSONObject();
        setCurrentStep(STEP_ORDER);
        try {
            JSONObject orderObject = new JSONObject();
            JSONArray products = new JSONArray();
            products.put(order.product.toJson());
            orderObject.put(Order.Api.PRODUCTS, products);
            orderObject.put(Order.Api.EXPECTED_PRICE_TOTAL, order.product.deliveryPrice + order.product.productPrice);
            orderObject.put(PaymentCard.Api.PAYMENT_CARD_ID, order.card.id);
            orderObject.put(Address.Api.ADDRESS_ID, order.address.id);
            params.put(Order.Api.ORDER, orderObject);
        } catch (JSONException e) {
            fireError(STEP_ORDER, null, null, e);
            return;
        }
        ShopeliaRestClient.authenticate(getContext());
        ShopeliaRestClient.post(Command.V1.Orders.$, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {

                if (httpResponse.getStatus() == 201) {
                    if (hasCallback()) {
                        getCallback().onOrderConfirmation(true);
                    }
                } else {
                    fireError(STEP_ORDER, httpResponse, null, new IllegalStateException(httpResponse.getBodyAsString()));
                }

            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                fireError(STEP_ORDER, null, null, e);
            }

        });
    }

}