package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopelia.android.utils.ParcelUtils;

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
    public static final Long NO_OPTION = null;
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
    private Long optionsHashcode = NO_OPTION;

    private Version(JSONObject object) throws JSONException {
        // Informations
        id = object.getLong(Api.ID);
        name = object.getString(Api.NAME);
        description = object.optString(Api.DESCRIPTION);
        shippingExtra = object.optString(Api.SHIPPING_EXTRAS);
        availabilityInfo = object.optString(Api.AVAILABILITY_INFO);
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
        optionsHashcode = ParcelUtils.readNullable(source, null, NO_OPTION);
    }

    public void setOptions(long optionsHash) {
        optionsHash = Long.valueOf(optionsHash);
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
        dest.writeString(imageUrl);

        // Prices
        dest.writeFloat(productPrice);
        dest.writeFloat(shippingPrice);
        dest.writeFloat(cashfrontValue);
        dest.writeFloat(priceStrikeOut);

        // Options
        ParcelUtils.writeNullable(dest, optionsHashcode);
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
        return 0;
    }

    public float getExpectedTotalPrice() {
        return 0;
    }

    public boolean isShippingFree() {
        return false;
    }

}
