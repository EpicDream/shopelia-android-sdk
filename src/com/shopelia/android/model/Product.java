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
        String QUANTITY = "quantity";
        String PRODUCT_VERSION_ID = "product_version_id";

    }

    public static final String IDENTIFIER = Product.class.getName();
    public static final float NO_PRICE = -1.f;

    public String url;

    public Merchant merchant;
    public Tax tax;
    public Currency currency;

    public final Versions versions;
    public long mCurrentVersionKey;

    private int mQuantity = 1;

    public Product(String url) {
        this.url = url;
        versions = new Versions();
    }

    protected Product(Parcel source) {
        url = source.readString();
        versions = new Versions();
        Version version = ParcelUtils.readParcelable(source, Version.class.getClassLoader());
        if (version != null) {
            mCurrentVersionKey = version.getOptionHashcode();
            versions.addVersion(version);
        }
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
        json.put(Api.PRODUCT_VERSION_ID, getCurrentVersion().getId());
        json.put(Api.QUANTITY, mQuantity);
        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        ParcelUtils.writeParcelable(dest, getCurrentVersion(), flags);
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
        mCurrentVersionKey = versions.getFirstKey();
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

    public void setCurrentVersion(long key) {
        mCurrentVersionKey = key;
    }

    public void setCurrentVersion(Option... options) {
        setCurrentVersion(Option.hashCode(options));
    }

    public void setCurrentVersion(int lastOptionChanged, Option... options) {
        if (versions.getVersion(options) == null) {
            int index = 0;
            for (; index < options.length; index++) {
                if (index != lastOptionChanged)
                    break;
            }
            setCurrentVersion(getAvailableOptions(index, lastOptionChanged, options).getOptions());
        } else {
            setCurrentVersion(options);
        }

    }

    private Version getAvailableOptions(int indexToChange, int lastIndexToChange, Option[] base) {
        Version version = null;
        Options options = versions.getOptions(indexToChange);
        for (Option option : options) {
            base[indexToChange] = option;
            version = versions.getVersion(base);
            if (version != null) {
                break;
            }
        }
        return version;
    }

    public Version getCurrentVersion() {
        return versions.getVersion(mCurrentVersionKey);
    }

    public boolean hasVersion() {
        return versions.getVersionsCount() > 0;
    }

    @Override
    public long getId() {
        return url.hashCode();
    }

    @Override
    public boolean isValid() {
        return hasVersion() && getCurrentVersion().isValid() && merchant != null;
    }

    public boolean isDone() {
        return false;
    }

}
