package com.shopelia.android.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Phone implements Parcelable {

    public static final long NO_ID = -1l;

    public static final int TYPE_LAND = 0;
    public static final int TYPE_MOBILE = 1;

    public interface Api {
        String ID = "id";
        String NUMBER = "number";
        String LINE_TYPE = "line_type";
        String ADDRESS_ID = "address_id";
    }

    public long id = NO_ID;
    public String number;
    public int type = TYPE_LAND;
    public long addressId = Address.NO_ID;

    public Phone() {

    }

    private Phone(Parcel source) {
        id = source.readLong();
        number = source.readString();
        type = source.readInt();
        addressId = source.readLong();
    }

    public static Phone inflate(JSONObject object) throws JSONException {
        Phone phone = new Phone();
        phone.id = object.getLong(Api.ID);
        phone.number = object.getString(Api.NUMBER);
        phone.type = object.getInt(Api.LINE_TYPE);
        phone.addressId = object.getLong(Api.ADDRESS_ID);
        return phone;
    }

    public static ArrayList<Phone> inflate(JSONArray array) throws JSONException {
        ArrayList<Phone> phones = new ArrayList<Phone>(array.length());
        final int size = array.length();
        for (int index = 0; index < size; index++) {
            phones.add(Phone.inflate(array.getJSONObject(index)));
        }
        return phones;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(number);
        dest.writeInt(type);
        dest.writeLong(addressId);
    }

    public static final Parcelable.Creator<Phone> CREATOR = new Creator<Phone>() {

        @Override
        public Phone[] newArray(int size) {
            return new Phone[size];
        }

        @Override
        public Phone createFromParcel(Parcel source) {
            return new Phone(source);
        }
    };

}
