package com.shopelia.android.remote.api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.config.Config;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class AddressesAPI extends ApiHandler {

    public AddressesAPI(Context context, Callback callback) {
        super(context, callback);
    }

    public void addAddress(Address address) {
        if (address == null) {
            return;
        }
        JSONObject params = new JSONObject();
        try {
            JSONObject addressObject = address.toJson();
            params.put(Order.Api.ADDRESS, addressObject);
            ShopeliaRestClient.authenticate(getContext());
            ShopeliaRestClient.post(Command.V1.Addresses.$, params, new AddressAsyncCallback(201) {

                @Override
                public void onSuccess(int status, JSONObject object, Address address) {
                    if (hasCallback()) {
                        getCallback().onAddressAdded(address);
                    }
                }

            });
        } catch (JSONException e) {
            if (Config.INFO_LOGS_ENABLED) {
                e.printStackTrace();
            }
        }
    }

    public void deleteAddress(long address_id) {

    }

    public void editAddress(Address address) {

    }

    private abstract class AddressAsyncCallback extends AsyncCallback {

        private List<Integer> mSuccessCode = new ArrayList<Integer>();

        public AddressAsyncCallback(int... successCodes) {
            for (int code : successCodes) {
                mSuccessCode.add(Integer.valueOf(code));
            }
        }

        @Override
        public void onComplete(HttpResponse httpResponse) {
            JSONObject object = null;
            try {
                object = new JSONObject(httpResponse.getBodyAsString());
            } catch (JSONException e) {
                onError(e);
            }
            if (mSuccessCode.contains(Integer.valueOf(httpResponse.getStatus()))) {
                try {
                    Address address = Address.inflate(object);
                    onSuccess(httpResponse.getStatus(), object, address);
                } catch (JSONException e) {
                    onError(e);
                }
            } else if (hasCallback()) {
                getCallback().onError(STEP_ADDRESS, httpResponse, object, null);
            }
        }

        public abstract void onSuccess(int status, JSONObject object, Address address);

        @Override
        public void onError(Exception e) {
            super.onError(e);
            if (hasCallback()) {
                getCallback().onError(STEP_ADDRESS, null, null, e);
            }
        }

    }

}
