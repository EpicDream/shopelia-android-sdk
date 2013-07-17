package com.shopelia.android.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.shopelia.android.config.Config;
import com.shopelia.android.utils.JsonUtils;
import com.shopelia.android.utils.ParcelUtils;

public class User implements BaseModel<User> {

    private static final String LOG_TAG = "Model$User";

    public interface Api {
        String DATA = "data";
        String USER = "user";
        String AUTH_TOKEN = "auth_token";

        String ID = "id";
        String EMAIL = "email";
        String PASSWORD = "password";
        String FIRST_NAME = "first_name";
        String LAST_NAME = "last_name";
        String ADDRESSES_ATTRIBUTES = "addresses_attributes";
        String ADDRESSES = "addresses";
        String PAYMENT_CARDS_ATTRIBUTES = "payment_cards_attributes";

        String PAYMENT_CARDS = "payment_cards";

        String PINCODE = "pincode";

        String CC_NUMBER = "cc_num";
        String CC_MONTH = "cc_month";
        String CC_YEAR = "cc_year";
    }

    public static final String IDENTIFIER = User.class.getName();

    public long id = NO_ID;
    public String email;
    public String firstname;
    public String lastname;
    public String password;
    public String pincode;

    public ArrayList<Address> addresses = new ArrayList<Address>();
    public ArrayList<PaymentCard> paymentCards = new ArrayList<PaymentCard>();

    public User() {

    }

    public User(JSONObject json) throws JSONException {
        this();
        id = json.getLong(Api.ID);
        email = json.getString(Api.EMAIL);
        firstname = json.getString(Api.FIRST_NAME);
        lastname = json.getString(Api.LAST_NAME);
    }

    @SuppressWarnings("unchecked")
    private User(Parcel source) {
        id = source.readLong();
        email = source.readString();
        firstname = source.readString();
        lastname = source.readString();
        pincode = source.readString();
        password = source.readString();
        ParcelUtils.readParcelableList(source, addresses, Address.class.getClassLoader());
        ParcelUtils.readParcelableList(source, paymentCards, PaymentCard.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(email);
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(pincode);
        dest.writeString(password);
        ParcelUtils.writeParcelableList(dest, addresses, flags);
        ParcelUtils.writeParcelableList(dest, paymentCards, flags);
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Api.ID, id);
        json.put(Api.EMAIL, email);
        json.put(Api.FIRST_NAME, firstname);
        json.put(Api.LAST_NAME, lastname);
        json.put(Api.ADDRESSES, JsonUtils.toJson(addresses));
        json.put(Api.PAYMENT_CARDS, JsonUtils.toJson(paymentCards));
        return json;
    }

    public void addAddress(Address address) {
        if (address.is_default) {
            for (Address item : addresses) {
                item.is_default = false;
            }
        }
        addresses.add(address);
    }

    public void addPaymentCard(PaymentCard card) {
        paymentCards.add(card);
    }

    public Address getDefaultAddress() {
        for (Address address : addresses) {
            if (address.is_default) {
                return address;
            }
        }
        return addresses.size() > 0 ? addresses.get(0) : null;
    }

    public PaymentCard getDefaultPaymentCard() {
        return paymentCards.size() > 0 ? paymentCards.get(0) : null;
    }

    public static User inflate(JSONObject json) {
        User user = new User();
        user.id = json.optLong(Api.ID, NO_ID);
        user.email = json.optString(Api.EMAIL);
        user.firstname = json.optString(Api.FIRST_NAME);
        user.lastname = json.optString(Api.LAST_NAME);
        user.password = json.optString(Api.PASSWORD);
        if (json.has(Api.ADDRESSES)) {
            try {
                user.addresses = Address.inflate(json.getJSONArray(Api.ADDRESSES));
            } catch (JSONException e) {
                if (Config.ERROR_LOGS_ENABLED) {
                    Log.w(LOG_TAG, "", e);
                }
            }
        }
        if (json.has(Api.PAYMENT_CARDS)) {
            try {
                user.paymentCards = PaymentCard.inflate(json.getJSONArray(Api.PAYMENT_CARDS));
            } catch (JSONException e) {
                if (Config.ERROR_LOGS_ENABLED) {
                    Log.w(LOG_TAG, "", e);
                }
            }
        }
        if (user.addresses == null) {
            user.addresses = new ArrayList<Address>();
        }
        if (user.paymentCards == null) {
            user.paymentCards = new ArrayList<PaymentCard>();
        }
        return user;
    }

    public static JSONObject createObjectForAccountCreation(User user, Address address, PaymentCard card) throws JSONException {
        user.firstname = address.firstname;
        user.lastname = address.lastname;
        JSONObject out = new JSONObject();
        out.put(User.Api.FIRST_NAME, user.firstname);
        out.put(User.Api.LAST_NAME, user.lastname);
        out.put(User.Api.EMAIL, user.email);
        out.put(Api.PINCODE, user.pincode);
        JSONArray addresses = new JSONArray();
        JSONObject addressObject = address.toJson();

        addresses.put(addressObject);
        out.put(User.Api.ADDRESSES_ATTRIBUTES, addresses);

        if (card != null) {
            JSONArray cards = new JSONArray();
            JSONObject cardObject = card.toJson();
            cards.put(cardObject);

            out.put(User.Api.PAYMENT_CARDS_ATTRIBUTES, cards);
        }
        return out;
    }

    public Bundle toUserdata(String authToken) {
        Bundle userdata = new Bundle();
        userdata.putString(Api.USER, email);
        userdata.putString(Api.FIRST_NAME, firstname);
        userdata.putString(Api.LAST_NAME, lastname);
        userdata.putString(Api.AUTH_TOKEN, authToken);
        try {
            userdata.putString(Api.USER, toJson().toString());
        } catch (JSONException e) {

        }
        return userdata;
    }

    @Override
    public int describeContents() {
        return 0;
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

    @Override
    public void merge(User item) {

    }

    @Override
    public long getId() {
        return id;
    }

}
