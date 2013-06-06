package com.shopelia.android.model;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public final class Address implements JsonData, Parcelable {

    public interface Api {
        String ADDRESS = "address";
        String ADDRESSES = "addresses";

        String ID = "id";
        String ADDRESS1 = "address1";
        String ZIP = "zip";
        String CITY = "city";
        String PHONE = "phone";
        String COUNTRY = "country";
        String EXTRAS = "address2";

        String COUNTRY_ISO = "country_iso";

        String ADDRESS_NAME = "name";

        String NAME = "last_name";
        String FIRSTNAME = "first_name";

        String REFERENCE = "reference";

        String ADDRESS_ID = "address_id";

    }

    public static final long NO_ID = -1;

    public long id = NO_ID;
    public String address;
    public String zipcode;
    public String city;
    public String country;
    public String phone;

    public String reference;

    public String name;
    public String firstname;
    public String extras;

    public Address() {

    }

    @SuppressWarnings("unchecked")
    private Address(Parcel source) {
        id = source.readLong();
        address = source.readString();
        zipcode = source.readString();
        city = source.readString();
        country = source.readString();
        reference = source.readString();
        name = source.readString();
        firstname = source.readString();
        phone = source.readString();
        extras = source.readString();
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (id != NO_ID) {
            json.put(Api.ID, id);
        }
        json.put(Api.NAME, name);
        json.put(Api.FIRSTNAME, firstname);
        if (reference == null) {
            json.put(Api.ADDRESS1, address);
            json.put(Api.ZIP, zipcode);
            json.put(Api.CITY, city);
            json.put(Api.COUNTRY, country);
        } else {
            json.put(Api.REFERENCE, reference);
        }
        if (!TextUtils.isEmpty(extras)) {
            json.put(Api.EXTRAS, extras);
        }
        json.put(Api.PHONE, phone);
        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(address);
        dest.writeString(zipcode);
        dest.writeString(city);
        dest.writeString(country);
        dest.writeString(reference);
        dest.writeString(name);
        dest.writeString(firstname);
        dest.writeString(phone);
        dest.writeString(extras);
    }

    public static Address inflate(JSONObject object) throws JSONException {
        Address address = new Address();
        address.id = object.optLong(Api.ID, NO_ID);
        address.name = object.optString(Api.NAME);
        address.firstname = object.optString(Api.FIRSTNAME);
        address.address = object.optString(Api.ADDRESS1);
        address.city = object.optString(Api.CITY);
        address.country = object.optString(Api.COUNTRY);
        address.zipcode = object.optString(Api.ZIP);
        address.phone = object.optString(Api.PHONE);
        address.reference = object.optString(Api.REFERENCE, null);
        if (TextUtils.isEmpty(address.country)) {
            address.country = Locale.getDefault().getCountry();
        }
        return address;
    }

    public static ArrayList<Address> inflate(JSONArray array) throws JSONException {
        ArrayList<Address> out = new ArrayList<Address>(array.length());
        final int size = array.length();
        for (int index = 0; index < size; index++) {
            out.add(Address.inflate(array.getJSONObject(index)));
        }
        return out;
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }

        @Override
        public Address createFromParcel(Parcel source) {
            return new Address(source);
        }
    };

    @Override
    public String toString() {
        return address;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(firstname)
                && !TextUtils.isEmpty(name)
                && (!TextUtils.isEmpty(reference) || (!TextUtils.isEmpty(address) && !TextUtils.isEmpty(country)
                        && !TextUtils.isEmpty(city) && !TextUtils.isEmpty(zipcode)));
    };

    public String getDisplayCountry() {
        return new Locale("", country).getDisplayCountry();
    }

}
