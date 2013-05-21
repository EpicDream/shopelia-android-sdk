package com.shopelia.android.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.Tax;

public class Product implements Parcelable {

    public String url;
    public String name;
    public String description;

    public Uri image;

    public Vendor vendor;
    public Tax tax;
    public Currency currency;

    public Product() {

    }

    private Product(Parcel source) {
        url = source.readString();
        name = source.readString();
        image = source.readParcelable(Uri.class.getClassLoader());
        description = source.readString();
        vendor = source.readParcelable(Vendor.class.getClassLoader());
        tax = source.readParcelable(Tax.class.getClassLoader());
        currency = source.readParcelable(Currency.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(name);
        dest.writeParcelable(image, flags);
        dest.writeString(description);
        dest.writeParcelable(vendor, flags);
        dest.writeParcelable(tax, flags);
        dest.writeParcelable(currency, flags);
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
