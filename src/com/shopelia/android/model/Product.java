package com.shopelia.android.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.ParcelUtils;
import com.shopelia.android.utils.Tax;

public class Product implements Parcelable {

    public String url;
    public String name;
    public String description;

    public Uri image;

    public Vendor vendor;
    public Tax tax;
    public Currency currency;

    // Shipping info
    public float productPrice;
    public float deliveryPrice;

    public String shippingExtra;

    public Product() {

    }

    private Product(Parcel source) {
        url = source.readString();
        name = source.readString();
        productPrice = source.readFloat();
        deliveryPrice = source.readFloat();
        shippingExtra = source.readString();
        image = ParcelUtils.readParcelable(source, Uri.class.getClassLoader());
        description = source.readString();
        vendor = ParcelUtils.readParcelable(source, Vendor.class.getClassLoader());
        tax = ParcelUtils.readParcelable(source, Tax.class.getClassLoader());
        currency = ParcelUtils.readParcelable(source, Currency.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(name);
        dest.writeFloat(productPrice);
        dest.writeFloat(deliveryPrice);
        dest.writeString(shippingExtra);
        ParcelUtils.writeParcelable(dest, image, flags);
        dest.writeString(description);
        ParcelUtils.writeParcelable(dest, vendor, flags);
        ParcelUtils.writeParcelable(dest, tax, flags);
        ParcelUtils.writeParcelable(dest, currency, flags);
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
