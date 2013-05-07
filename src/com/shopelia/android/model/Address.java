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
        String PHONES_ATTRIBUTES = "phones_attributes";
        String PHONES = "phones";
        String COUNTRY = "country";
        String EXTRAS = "address2";

        String COUNTRY_ISO = "country_iso";

        String ADDRESS_NAME = "name";

        String NAME = "last_name";
        String FIRSTNAME = "first_name";

        String REFERENCE = "reference";
    }

    public static final long NO_ID = -1;

    public long id = NO_ID;
    public String address;
    public String zipcode;
    public String city;
    public String country;
    public ArrayList<Phone> phones = new ArrayList<Phone>();

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
        extras = source.readString();
        phones = source.readArrayList(Phone.class.getClassLoader());
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
        dest.writeString(extras);
        dest.writeTypedList(phones);
    }

    public static Address inflate(JSONObject object) throws JSONException {
        Address address = new Address();
        address.name = object.optString(Api.NAME);
        address.firstname = object.optString(Api.FIRSTNAME);
        address.address = object.optString(Api.ADDRESS1);
        address.city = object.optString(Api.CITY);
        address.country = object.optString(Api.COUNTRY);
        address.zipcode = object.optString(Api.ZIP);
        address.reference = object.optString(Api.REFERENCE, null);
        if (TextUtils.isEmpty(address.country)) {
            address.country = Locale.getDefault().getCountry();
        }
        if (object.has(Api.PHONES)) {
            address.phones = Phone.inflate(object.getJSONArray(Api.PHONES));
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

}
