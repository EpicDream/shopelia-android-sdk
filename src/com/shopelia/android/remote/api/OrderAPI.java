package com.shopelia.android.remote.api;

import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.shopelia.android.config.Config;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.PaymentCard;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class OrderAPI extends ApiController {

    public static class OnOrderConfirmationEvent {

    }

    public static class OnInvalidOrderRequestEvent {
        public final String message;

        private OnInvalidOrderRequestEvent(String message) {
            this.message = message;
        }

    }

    private static Class<?>[] sEventTypes = new Class<?>[] {
            OnOrderConfirmationEvent.class, OnInvalidOrderRequestEvent.class
    };

    public OrderAPI(Context context) {
        super(context);
    }

    public void order(final Order order, boolean test) {

        JSONObject params = new JSONObject();
        try {
            JSONObject orderObject = new JSONObject();
            JSONArray products = new JSONArray();
            products.put(order.product.toJson());
            orderObject.put(Order.Api.PRODUCTS, products);
            orderObject.put(Order.Api.EXPECTED_PRICE_TOTAL,
                    round(order.product.getCurrentVersion().getExpectedTotalPrice(), 2, BigDecimal.ROUND_HALF_UP));
            orderObject.put(Order.Api.EXPECTED_CASHFRONT_VALUE,
                    round(order.product.getCurrentVersion().getExpectedCashfrontValue(), 2, BigDecimal.ROUND_HALF_UP));
            orderObject.put(PaymentCard.Api.PAYMENT_CARD_ID, order.card.id);
            orderObject.put(Address.Api.ADDRESS_ID, order.address.id);
            if (test) {
                orderObject.put(Order.Api.EXPECTED_PRICE_TOTAL, 1);
            }
            params.put(Order.Api.ORDER, orderObject);
            if (Config.INFO_LOGS_ENABLED) {
                Log.d(null, "ORDER " + params.toString(2));
            }
        } catch (JSONException e) {
            fireError(null, null, e);
            return;
        }
        ShopeliaRestClient.V1(getContext()).post(Command.V1.Orders.$, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {

                if (httpResponse.getStatus() == 201) {
                    getEventBus().post(new OnOrderConfirmationEvent());
                } else if (httpResponse.getStatus() == 422) {
                    getEventBus().post(new OnInvalidOrderRequestEvent(ErrorInflater.grabErrorMessage(httpResponse.getBodyAsString())));
                } else {
                    fireError(httpResponse, null, new IllegalStateException(httpResponse.getBodyAsString()));
                }

            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                fireError(null, null, e);
            }

        });
    }

    public void order(final Order order) {
        order(order, false);
    }

    @Override
    public Class<?>[] getEventTypes() {
        return sEventTypes;
    }

    private static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

}
