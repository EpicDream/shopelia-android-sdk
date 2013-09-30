package com.shopelia.android.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.ParcelUtils;
import com.shopelia.android.utils.Tax;

public class Product implements BaseModel<Product> {

    public interface Api {
        String URL = "url";
        String MERCHANT = "merchant";
        String VERSIONS = "versions";
    }

    public static final String IDENTIFIER = Product.class.getName();
    public static final float NO_PRICE = -1.f;

    public String url;

    public Merchant merchant;
    public Tax tax;
    public Currency currency;

    public final Versions versions;

    public Product(String url) {
        this.url = url;
        versions = new Versions();
    }

    protected Product(Parcel source) {
        url = source.readString();
        Versions v = ParcelUtils.readParcelable(source, Versions.class.getClassLoader());
        versions = v != null ? v : new Versions();
        merchant = ParcelUtils.readParcelable(source, Merchant.class.getClassLoader());
        tax = ParcelUtils.readParcelable(source, Tax.class.getClassLoader());
        currency = ParcelUtils.readParcelable(source, Currency.class.getClassLoader());
    }

    public float getTotalPrice() {
        return getCurrentVersion().getTotalPrice();
    }

    public float getExpectedTotalPrice() {
        return getCurrentVersion().getExpectedTotalPrice();
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        ParcelUtils.writeParcelable(dest, versions, flags);
        ParcelUtils.writeParcelable(dest, merchant, flags);
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

    public Product(JSONObject object) throws JSONException {
        url = object.optString(Api.URL);
        if (object.has(Api.MERCHANT)) {
            merchant = Merchant.inflate(object.getJSONObject(Api.MERCHANT));
        }
        versions = Versions.inflate(object.getJSONArray(Api.VERSIONS));
        ensureDefaultValues();
    }

    public static Product inflate(JSONObject object) throws JSONException {
        return new Product(object);
    }

    public static ArrayList<Product> inflate(JSONArray a) throws JSONException {
        final int size = a.length();
        ArrayList<Product> products = new ArrayList<Product>(size);
        for (int index = 0; index < size; index++) {
            products.add(Product.inflate(a.getJSONObject(index)));
        }
        return products;
    }

    protected void ensureDefaultValues() {
        if (currency == null) {
            currency = Currency.EUR;
        }

        if (tax == null) {
            tax = Tax.ATI;
        }

    }

    @Override
    @Deprecated
    public void merge(Product cpy) {

    }

    public Version getCurrentVersion() {
        return null;
    }

    @Override
    public long getId() {
        return url.hashCode();
    }

    @Override
    public boolean isValid() {
        return getCurrentVersion().isValid() && merchant != null;
    }

}
