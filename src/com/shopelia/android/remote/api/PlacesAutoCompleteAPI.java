package com.shopelia.android.remote.api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;
import android.util.Log;

import com.shopelia.android.config.Config;
import com.shopelia.android.model.Address;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

public final class PlacesAutoCompleteAPI {

    public interface OnAddressDetailsListener {
        public void onAddressDetails(Address address);

        public void onError();
    }

    public static final String LOG_TAG = "PlacesAutocompleteClient";

    private static class Command {

        static String $ = "/api/places/";

        interface Autocomplete {
            String $ = Command.$ + "autocomplete";

            String QUERY = "query";
            String LATITUDE = "lat";
            String LONGITUDE = "lng";
        }

        public static String Details(String reference) {
            return Command.$ + "details/" + reference;
        }
    }

    private interface Api {
        interface Autocomplete {
            String DESCRIPTION = "description";
            String REFERENCE = Address.Api.REFERENCE;
        }
    }

    private PlacesAutoCompleteAPI() {

    }

    public static List<Address> autocomplete(final Context context, final String input, final int cursorOffset) {

        if (TextUtils.isEmpty(input) || input.length() < 3) {
            return null;
        }

        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location == null) {
            location = new Location(LocationManager.NETWORK_PROVIDER);
        }

        ParameterMap params = new ParameterMap();
        params.add(Command.Autocomplete.QUERY, input);
        params.add(Command.Autocomplete.LATITUDE, String.valueOf(location.getLatitude()));
        params.add(Command.Autocomplete.LONGITUDE, String.valueOf(location.getLongitude()));
        HttpResponse httpResponse = ShopeliaRestClient.V1(context).get(Command.Autocomplete.$, params);
        try {
            Log.d(null, httpResponse.getBodyAsString());
            List<Address> addresses = inflatesAutocompletionAddresses(new JSONArray(httpResponse.getBodyAsString()));
            return addresses;
        } catch (JSONException e) {
            if (Config.ERROR_LOGS_ENABLED) {
                Log.e(LOG_TAG, "Unexpected JSON exception ", e);
            }
        }

        return null;
    }

    public static void getAddressDetails(final Context context, String reference, final OnAddressDetailsListener listener) {
        ShopeliaRestClient.V1(context).get(Command.Details(reference), null, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {
                try {
                    Address address = Address.inflate(new JSONObject(httpResponse.getBodyAsString()));
                    listener.onAddressDetails(address);
                } catch (JSONException e) {
                    if (Config.ERROR_LOGS_ENABLED) {
                        Log.e(LOG_TAG, "Unexpected JSON exception", e);
                    }
                    onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                listener.onError();
            }

        });
    }

    private static List<Address> inflatesAutocompletionAddresses(JSONArray array) throws JSONException {
        ArrayList<Address> addresses = new ArrayList<Address>(array.length());
        final int count = array.length();
        for (int index = 0; index < count; index++) {
            JSONObject entry = array.getJSONObject(index);
            Address address = new Address();
            address.address = entry.getString(Api.Autocomplete.DESCRIPTION);
            address.reference = entry.getString(Api.Autocomplete.REFERENCE);
            addresses.add(address);
        }
        return addresses;
    }
}
