package com.shopelia.android.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Order implements Parcelable {

    public String productUrl;

    // Shipping
    public Address address;

    // Payment card

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub

    }

}
