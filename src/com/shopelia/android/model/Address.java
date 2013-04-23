package com.shopelia.android.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public final class Address implements JsonData, Parcelable {

    public interface Api {
        String ADDRESS = "address";
        String ADDRESSES = "addresses";

        String ID = "id";
        String ADDRESS1 = "address1";
        String ZIP = "zip";
        String CITY = "city";
        String PHONES_ATTRIBUTES = "phones_attributes";
    }

    public long id;
    public String address;
    public String zipcode;
    public String city;
    public String country;
    public ArrayList<Phone> phones;

    public String reference;

    public Address() {

    }

    private Address(Parcel source) {
        id = source.readLong();
        address = source.readString();
        zipcode = source.readString();
        city = source.readString();
        country = source.readString();
        reference = source.readString();
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Api.ID, id);
        json.put(Api.ADDRESS1, address);
        json.put(Api.ZIP, zipcode);
        json.put(Api.CITY, city);
        if (phones.size() > 0) {
            JSONArray array = new JSONArray();
            for (Phone phone : phones) {
                // array.put(phone.toJSONObject());
            }
            json.put(Api.PHONES_ATTRIBUTES, array);
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

    public String toString() {
        return address;
    };

}