package com.shopelia.android.model;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract.CommonDataKinds.Phone;
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
        String COUNTRY = "country";
        String EXTRAS = "extras";
    }

    private static final long INVALID_ID = -1;

    public long id = INVALID_ID;
    public String address;
    public String zipcode;
    public String city;
    public String country;
    public ArrayList<Phone> phones;

    public String reference;

    public String name;
    public String firstname;
    public String extras;

    public Address() {

    }

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
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (id != INVALID_ID) {
            json.put(Api.ID, id);
        }
        json.put(Api.ADDRESS1, address);
        json.put(Api.ZIP, zipcode);
        json.put(Api.CITY, city);
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
    }

    public static Address inflate(JSONObject object) throws JSONException {
        Address address = new Address();
        address.address = object.getString(Api.ADDRESS1);
        address.city = object.getString(Api.CITY);
        address.country = object.getString(Api.COUNTRY);
        address.zipcode = object.getString(Api.ZIP);
        return address;
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
    };

}
