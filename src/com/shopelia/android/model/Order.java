package com.shopelia.android.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Order implements Parcelable {

    public String productUrl;

    // Shipping
    public Address address;

    // Payment card

    public Order() {

    }

    private Order(Parcel source) {

    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
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

}
