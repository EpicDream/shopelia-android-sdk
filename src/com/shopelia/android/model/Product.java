package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopelia.android.api.Shopelia;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.ParcelUtils;
import com.shopelia.android.utils.Tax;

public class Product implements BaseModel<Product> {

    public interface Api {
        String NAME = "name";
        String URL = "url";
        String IMAGE_URL = "image_url";
        String EXPECTED_PRODUCT_PRICE = "expected_price_product";
        String EXPECTED_SHIPPING_PRICE = "expected_price_shipping";
        String SHIPPING_EXTRAS = "shipping_info";
        String MERCHANT = "merchant";
    }

    public static final String IDENTIFIER = Product.class.getName();
    public static final float NO_PRICE = -1.f;

    public String url;
    public String name;
    public String description;

    public Uri image;

    public Merchant merchant;
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
        merchant = ParcelUtils.readParcelable(source, Merchant.class.getClassLoader());
        tax = ParcelUtils.readParcelable(source, Tax.class.getClassLoader());
        currency = ParcelUtils.readParcelable(source, Currency.class.getClassLoader());
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Api.NAME, name);
        json.put(Api.URL, url);
        json.put(Api.IMAGE_URL, image.toString());
        return json;
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

    public static Product inflate(JSONObject object) throws JSONException {
        Product product = new Product();
        product.name = object.optString(Api.NAME);
        product.url = object.optString(Api.URL);
        product.image = Uri.parse(object.optString(Api.IMAGE_URL));
        if (object.has(Api.MERCHANT)) {
            product.merchant = Merchant.inflate(object.getJSONObject(Api.MERCHANT));
        }
        product.deliveryPrice = (float) object.optDouble(Api.EXPECTED_SHIPPING_PRICE);
        product.productPrice = (float) object.optDouble(Api.EXPECTED_PRODUCT_PRICE);
        product.shippingExtra = object.optString(Api.SHIPPING_EXTRAS);
        product.ensureDefaultValues();
        return product;
    }

    public static Product inflate(Bundle bundle) {
        Product product = new Product();
        product.name = bundle.getString(Shopelia.EXTRA_PRODUCT_TITLE);
        product.image = bundle.getParcelable(Shopelia.EXTRA_PRODUCT_IMAGE);
        product.url = bundle.getString(Shopelia.EXTRA_PRODUCT_URL);
        product.description = bundle.getString(Shopelia.EXTRA_PRODUCT_DESCRIPTION);
        product.currency = bundle.getParcelable(Shopelia.EXTRA_CURRENCY);
        product.deliveryPrice = bundle.getFloat(Shopelia.EXTRA_SHIPPING_PRICE, NO_PRICE);
        product.merchant = bundle.getParcelable(Shopelia.EXTRA_MERCHANT);
        product.tax = bundle.getParcelable(Shopelia.EXTRA_TAX);
        product.shippingExtra = bundle.getString(Shopelia.EXTRA_SHIPPING_INFO);
        product.productPrice = bundle.getFloat(Shopelia.EXTRA_PRICE, NO_PRICE);
        product.ensureDefaultValues();
        return product;
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
    public void merge(Product item) {
        // TODO Auto-generated method stub
    }

    @Override
    public long getId() {
        return url.hashCode();
    }

}
