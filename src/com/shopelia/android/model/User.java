package com.shopelia.android.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements JsonData, Parcelable {

    public interface Api {
        String USER = "user";
        String AUTH_TOKEN = "auth_token";

        String ID = "id";
        String EMAIL = "email";
        String PASSWORD = "password";
        String FIRST_NAME = "first_name";
        String LAST_NAME = "last_name";
        String ADDRESSES_ATTRIBUTES = "addresses_attributes";
        String ADDRESSES = "addresses";

        String PAYMENT_CARDS = "payment_cards";

        String PINCODE = "pincode";

        String PHONES = "phones";
        String PHONE = "phone";

        public interface Phone {
            String NUMBER = "number";
            String LINE_TYPE = "line_type";
        }
    }

    public static final long NO_ID = -1;

    public long id = NO_ID;
    public String email;
    public String firstName;
    public String lastName;
    public String phone;
    public String pincode;

    public ArrayList<Address> addresses = new ArrayList<Address>();
    public ArrayList<PaymentCard> paymentCards = new ArrayList<PaymentCard>();

    public User() {

    }

    public User(JSONObject json) throws JSONException {
        this();
        id = json.getLong(Api.ID);
        email = json.getString(Api.EMAIL);
        firstName = json.getString(Api.FIRST_NAME);
        lastName = json.getString(Api.LAST_NAME);
    }

    @SuppressWarnings("unchecked")
    private User(Parcel source) {
        id = source.readLong();
        email = source.readString();
        firstName = source.readString();
        lastName = source.readString();
        phone = source.readString();
        addresses = source.readArrayList(Address.class.getClassLoader());
        paymentCards = source.readArrayList(PaymentCard.class.getClassLoader());
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Api.ID, id);
        json.put(Api.EMAIL, email);
        json.put(Api.FIRST_NAME, firstName);
        json.put(Api.LAST_NAME, lastName);
        return json;
    }

    public static User inflate(JSONObject json) {
        User user = new User();
        user.id = json.optLong(Api.ID, NO_ID);
        user.email = json.optString(Api.EMAIL);
        user.firstName = json.optString(Api.FIRST_NAME);
        user.lastName = json.optString(Api.LAST_NAME);
        user.phone = json.optString(Api.PHONE);
        if (json.has(Api.ADDRESSES)) {
            try {
                user.addresses = Address.inflate(json.getJSONArray(Api.ADDRESSES));
            } catch (JSONException e) {
                user.addresses = new ArrayList<Address>();
            }
        }
        if (json.has(Api.PAYMENT_CARDS)) {
            try {
                user.paymentCards = PaymentCard.inflate(json.getJSONArray(Api.PAYMENT_CARDS));
            } catch (JSONException e) {

            }
        }
        return user;
    }

    public static JSONObject createObjectForAccountCreation(User user, Address address) throws JSONException {
        user.firstName = address.firstname;
        user.lastName = address.name;
        JSONObject out = new JSONObject();
        out.put(User.Api.FIRST_NAME, user.firstName);
        out.put(User.Api.LAST_NAME, user.lastName);
        out.put(User.Api.EMAIL, user.email);
        JSONArray addresses = new JSONArray();
        JSONObject addressObject = address.toJson();

        addressObject.remove(Address.Api.FIRSTNAME);
        addressObject.remove(Address.Api.NAME);
        addressObject.remove(Address.Api.COUNTRY);
        // TODO Handle multiple ISO
        addressObject.put(Address.Api.COUNTRY_ISO, "FR");

        out.put(User.Api.ADDRESSES_ATTRIBUTES, addresses);
        JSONArray phones = new JSONArray();
        JSONObject phoneObject = new JSONObject();
        phoneObject.put(Api.Phone.LINE_TYPE, 0);
        phoneObject.put(Api.Phone.NUMBER, user.phone);
        phones.put(phoneObject);

        addressObject.put(Address.Api.PHONES_ATTRIBUTES, phones);

        addresses.put(addressObject);

        return out;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(email);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(phone);
        dest.writeTypedList(addresses);
        dest.writeTypedList(paymentCards);
    }

    public static final Parcelable.Creator<User> CREATOR = new Creator<User>() {

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }

        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }
    };

}
