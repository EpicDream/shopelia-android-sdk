package com.shopelia.android.api;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.shopelia.android.config.Config;
import com.shopelia.android.model.Address;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

public final class PlacesAutoCompleteClient {

    public interface OnAddressesAvailableListener {
        public void onAddressesAvailable(List<Address> addresses);
    }

    public static final String LOG_TAG = "PlacesAutocompleteClient";

    private interface Command {

        String $ = "/api/places/";

        interface Autocomplete {
            String $ = Command.$ + "autocomplete";

            String QUERY = "query";
            String LATITUDE = "lat";
            String LONGITUDE = "lng";
        }

    }

    private interface Api {
        interface Autocomplete {
            String DESCRIPTION = "description";
            String REFERENCE = "reference";
        }
    }

    private PlacesAutoCompleteClient() {

    }

    public static List<Address> autocomplete(final Context context, final String input, final int cursorOffset) {

        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null) {

            if (TextUtils.isEmpty(input)) {
                return null;
            }

            ParameterMap params = ShopeliaRestClient.newParams();
            params.add(Command.Autocomplete.QUERY, input);
            params.add(Command.Autocomplete.LATITUDE, String.valueOf(location.getLatitude()));
            params.add(Command.Autocomplete.LONGITUDE, String.valueOf(location.getLongitude()));

            HttpResponse httpResponse = ShopeliaRestClient.get(Command.Autocomplete.$, params);
            try {
                List<Address> addresses = inflatesAutocompletionAddresses(new JSONArray(httpResponse.getBodyAsString()));
                return addresses;
            } catch (JSONException e) {
                if (Config.ERROR_LOGS_ENABLED) {
                    Log.e(LOG_TAG, "Unexpected JSON exception ", e);
                }
            }
        } else {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }

                @Override
                public void onLocationChanged(Location location) {
                    lm.removeUpdates(this);
                }
            });
        }
        return null;
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
