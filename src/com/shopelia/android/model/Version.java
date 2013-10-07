package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Version implements BaseModel<Version> {

    public interface Api {
        String ID = "id";
        String NAME = "name";
        String DESCRIPTION = "description";
        String IMAGE_URL = "image_url";
        String PRODUCT_PRICE = "price";
        String SHIPPING_PRICE = "price_shipping";
        String SHIPPING_EXTRAS = "shipping_info";
        String CASHFRONT_VALUE = "cashfront_value";
        String PRICE_STRIKEOUT = "price_strikeout";
        String AVAILABILITY_INFO = "availability_info";
    }

    public static final float NO_PRICE = -1;
    public static final long FIRST_OPTION = 0;
    private final long id;

    // Prices
    public final float productPrice;
    public final float shippingPrice;
    public final float cashfrontValue;
    public final float priceStrikeOut;

    // Informations
    public final String name;
    public final String imageUrl;
    public final String description;
    public final String shippingExtra;
    public final String availabilityInfo;

    // Options
    private long optionsHashcode = FIRST_OPTION;
    private Option[] options;

    private Version(JSONObject object) throws JSONException {
        // Informations
        id = object.getLong(Api.ID);
        name = object.getString(Api.NAME);
        description = object.optString(Api.DESCRIPTION);
        shippingExtra = object.optString(Api.SHIPPING_EXTRAS);
        availabilityInfo = shippingExtra == null || !shippingExtra.equalsIgnoreCase(object.optString(Api.AVAILABILITY_INFO)) ? object
                .optString(Api.AVAILABILITY_INFO) : null;
        imageUrl = object.optString(Api.IMAGE_URL);

        // Prices informations
        productPrice = (float) object.optDouble(Api.PRODUCT_PRICE, NO_PRICE);
        shippingPrice = (float) object.optDouble(Api.SHIPPING_PRICE, NO_PRICE);
        cashfrontValue = (float) object.optDouble(Api.CASHFRONT_VALUE, NO_PRICE);
        priceStrikeOut = (float) object.optDouble(Api.PRICE_STRIKEOUT, NO_PRICE);

    }

    private Version(Parcel source) {
        // Informations
        id = source.readLong();
        name = source.readString();
        description = source.readString();
        shippingExtra = source.readString();
        availabilityInfo = source.readString();
        imageUrl = source.readString();

        // Prices
        productPrice = source.readFloat();
        shippingPrice = source.readFloat();
        cashfrontValue = source.readFloat();
        priceStrikeOut = source.readFloat();

        // Options
        optionsHashcode = source.readLong();
        Parcelable[] p = source.readParcelableArray(Option.class.getClassLoader());
        options = new Option[p.length];
        for (int index = 0; index < p.length; index++) {
            options[index] = (Option) p[index];
        }
    }

    public void setOptions(long optionsHash, Option[] options) {
        optionsHash = Long.valueOf(optionsHash);
        this.options = options;
    }

    public Option[] getOptions() {
        return options;
    }

    public long getOptionHashcode() {
        return optionsHashcode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Uri getImageUri() {
        return Uri.parse(imageUrl);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Informations
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(shippingExtra);
        dest.writeString(availabilityInfo);
        dest.writeString(imageUrl);

        // Prices
        dest.writeFloat(productPrice);
        dest.writeFloat(shippingPrice);
        dest.writeFloat(cashfrontValue);
        dest.writeFloat(priceStrikeOut);

        // Options
        dest.writeLong(optionsHashcode);
        dest.writeParcelableArray(options, flags);
    }

    public static Version inflate(JSONObject object) throws JSONException {
        return new Version(object);
    }

    @Override
    public JSONObject toJson() throws JSONException {
        return null;
    }

    @Override
    public void merge(Version item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean isValid() {
        return productPrice != NO_PRICE && shippingPrice != NO_PRICE;
    }

    public static final Parcelable.Creator<Version> CREATOR = new Creator<Version>() {

        @Override
        public Version createFromParcel(Parcel source) {
            return new Version(source);
        }

        @Override
        public Version[] newArray(int size) {
            return new Version[size];
        }

    };

    // Price utility methods
    public float getTotalPrice() {
        return (centify(productPrice) + centify(shippingPrice) - centify(cashfrontValue)) / 100.f;
    }

    private int centify(float price) {
        return (int) (price != NO_PRICE ? price * 100 : 0);
    }

    public float getExpectedTotalPrice() {
        return (centify(productPrice) + centify(shippingPrice)) / 100.f;
    }

    public boolean isShippingFree() {
        return shippingPrice <= 0.f;
    }

    public double getExpectedCashfrontValue() {
        return (centify(cashfrontValue)) / 100.f;
    }

}
