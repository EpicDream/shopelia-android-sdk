package com.shopelia.android.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {

    public String url;
    public String name;

    public Product() {

    }

    private Product(Parcel source) {
        url = source.readString();
        name = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(name);
    }

    public static final Parcelable.Creator<Product> CREATOR = new Creator<Product>() {

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }

        @Override
        public Product createFromParcel(Parcel source) {
            return new Product(source);
        }
    };

}
