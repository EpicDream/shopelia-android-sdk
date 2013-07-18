package com.shopelia.android.remote.api;

import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.PaymentCard;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class OrderAPI extends ApiHandler {

    public OrderAPI(Context context, Callback callback) {
        super(context, callback);
    }

    private double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

    public void order(final Order order, boolean test) {

        JSONObject params = new JSONObject();
        setCurrentStep(STEP_ORDER);
        try {
            JSONObject orderObject = new JSONObject();
            JSONArray products = new JSONArray();
            products.put(order.product.toJson());
            orderObject.put(Order.Api.PRODUCTS, products);
            orderObject.put(Order.Api.EXPECTED_PRICE_TOTAL,
                    round(order.product.deliveryPrice + order.product.productPrice, 2, BigDecimal.ROUND_HALF_UP));
            orderObject.put(PaymentCard.Api.PAYMENT_CARD_ID, order.card.id);
            orderObject.put(Address.Api.ADDRESS_ID, order.address.id);
            if (test) {
                orderObject.put(Order.Api.EXPECTED_PRICE_TOTAL, 1);
            }
            params.put(Order.Api.ORDER, orderObject);
        } catch (JSONException e) {
            fireError(STEP_ORDER, null, null, e);
            return;
        }
        ShopeliaRestClient.V1(getContext()).post(Command.V1.Orders.$, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {

                if (httpResponse.getStatus() == 201) {
                    if (hasCallback()) {
                        getCallback().onOrderConfirmation(true);
                    }
                } else if (httpResponse.getStatus() == 422) {
                    if (hasCallback()) {
                        getCallback().onInvalidOrderRequest(ErrorInflater.grabErrorMessage(httpResponse.getBodyAsString()));
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

    public void order(final Order order) {
        order(order, false);
    }

}
