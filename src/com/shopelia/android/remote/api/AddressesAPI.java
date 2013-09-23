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

public class AddressesAPI extends ApiController {

    public static class OnRequestDone {

    }

    public static class OnAddressEvent extends OnAddResourceEvent<Address> {

        public OnAddressEvent(Address resource) {
            super(resource);
        }

    }

    public static class OnEditAddressEvent extends OnEditResourceEvent<Address> {

        public OnEditAddressEvent(Address resource) {
            super(resource);
        }

    }

    private static Class<?>[] sEventTypes = new Class<?>[] {
            OnRequestDone.class, OnAddressEvent.class, OnEditAddressEvent.class, OnApiErrorEvent.class
    };

    public AddressesAPI(Context context) {
        super(context);
    }

    public void addAddress(Address address) {
        if (address == null) {
            return;
        }
        JSONObject params = new JSONObject();
        try {
            JSONObject addressObject = address.toJson();
            params.put(Order.Api.ADDRESS, addressObject);
            ShopeliaRestClient.V1(getContext()).post(Command.V1.Addresses.$, params, new AddressAsyncCallback(201) {

                @Override
                public void onSuccess(int status, JSONObject object, Address address) {
                    getEventBus().post(new OnAddressEvent(address));
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

    public void editAddress(final Address address) {
        if (address == null) {
            return;
        }
        JSONObject params = new JSONObject();
        try {
            JSONObject addressObject = address.toJson();
            params.put(Order.Api.ADDRESS, addressObject);
            ShopeliaRestClient.V1(getContext()).put(Command.V1.Addresses.Address(address.id), params, new AddressAsyncCallback(204) {

                @Override
                public void onSuccess(int status, JSONObject object, Address a) {
                    getEventBus().post(new OnEditAddressEvent(a));
                }

            });
        } catch (JSONException e) {
            if (Config.INFO_LOGS_ENABLED) {
                e.printStackTrace();
            }
        }
    }

    public void setDefaultAddress(final Address address) {
        if (address == null) {
            return;
        }
        JSONObject params = new JSONObject();
        try {
            params.put(Address.Api.IS_DEFAULT, 1);
            ShopeliaRestClient.V1(getContext()).put(Command.V1.Addresses.Address(address.id), params, new AsyncCallback() {

                @Override
                public void onComplete(HttpResponse httpResponse) {
                    getEventBus().post(new OnRequestDone());
                }

                @Override
                public void onError(Exception e) {
                    super.onError(e);
                    fireError(null, null, e);
                }

            });
        } catch (JSONException e) {
            // Do nothing
        }
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
                onSuccess(httpResponse.getStatus(), object, null);
                return;
            }
            if (mSuccessCode.contains(Integer.valueOf(httpResponse.getStatus()))) {
                try {
                    Address address = Address.inflate(object.getJSONObject(Address.Api.ADDRESS));
                    onSuccess(httpResponse.getStatus(), object, address);
                } catch (JSONException e) {

                }
            } else {
                fireError(httpResponse, object, null);
            }
        }

        public abstract void onSuccess(int status, JSONObject object, Address address);

        @Override
        public void onError(Exception e) {
            super.onError(e);
            fireError(null, null, e);
        }

    }

    @Override
    public Class<?>[] getEventTypes() {
        return sEventTypes;
    }

}
