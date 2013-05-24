package com.shopelia.android.remote.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.model.Order;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class OrderCommandHandler extends CommandHandler {

    public OrderCommandHandler(Context context, Callback callback) {
        super(context, callback);
    }

    public void order(final Order order) {

        JSONObject params = new JSONObject();
        setCurrentStep(STEP_ORDER);
        try {
            JSONObject orderObject = new JSONObject();
            JSONArray urls = new JSONArray();
            urls.put(order.product.url);
            orderObject.put(Order.Api.PRODUCT_URLS, urls);
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
