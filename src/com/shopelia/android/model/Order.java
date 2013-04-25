package com.shopelia.android.model;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Order implements Parcelable {

    public interface Api {
        String UUID = "uuid";
        String USER = "user";
        String ADDRESS = "address";
        String PAYMENT_CARD = "payment_card";
        String PRODUCT_URL = "product_url";
    }

    public static final long NO_ID = -1;

    public long uuid = NO_ID;

    public String productUrl;

    // Shipping
    public Address address;

    // Payment card
    public PaymentCard card;

    // User
    public User user;

    public Order() {

    }

    private Order(Parcel source) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }

        @Override
        public Order createFromParcel(Parcel source) {
            return new Order(source);
        }
    };

    public static Order inflate(JSONObject object) {
        Order order = new Order();
        order.uuid = object.optLong(Api.UUID, NO_ID);
        order.productUrl = object.optString(Api.PRODUCT_URL);
        return order;
    }
}
