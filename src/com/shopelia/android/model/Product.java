package com.shopelia.android.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.shopelia.android.api.Shopelia;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.ParcelUtils;
import com.shopelia.android.utils.Tax;

public class Product implements BaseModel<Product> {

    public interface Api {
        String NAME = "name";
        String URL = "url";
        String IMAGE_URL = "image_url";
        String EXPECTED_PRODUCT_PRICE = "price";
        String EXPECTED_SHIPPING_PRICE = "price_shipping";
        String SHIPPING_EXTRAS = "shipping_info";
        String MERCHANT = "merchant";
        String VERSIONS = "versions";
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

    public ArrayList<Product> versions = new ArrayList<Product>();

    public Product() {

    }

    public Product(Product cpy) {
        merge(cpy);
        ensureDefaultValues();
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

    public Product inflateSelf(JSONObject object) throws JSONException {
        name = object.optString(Api.NAME);
        url = object.optString(Api.URL);
        image = Uri.parse(object.optString(Api.IMAGE_URL));
        if (object.has(Api.MERCHANT)) {
            merchant = Merchant.inflate(object.getJSONObject(Api.MERCHANT));
        }
        deliveryPrice = (float) object.optDouble(Api.EXPECTED_SHIPPING_PRICE, 12);
        productPrice = (float) object.optDouble(Api.EXPECTED_PRODUCT_PRICE, 12);
        shippingExtra = object.optString(Api.SHIPPING_EXTRAS);
        if (object.has(Api.VERSIONS)) {
            versions = inflate(this, object.getJSONArray(Api.VERSIONS));
            if (versions.size() > 0) {
                merge(versions.get(0));
            }
        }
        ensureDefaultValues();
        return this;
    }

    public static Product inflate(JSONObject object) throws JSONException {
        return new Product().inflateSelf(object);
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

    public static ArrayList<Product> inflate(Product base, JSONArray array) throws JSONException {
        final int size = array.length();
        ArrayList<Product> products = new ArrayList<Product>(size);
        for (int index = 0; index < size; index++) {
            products.add(new Product(base).inflateSelf(array.getJSONObject(index)));
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
    public void merge(Product cpy) {
        if (cpy != null) {
            url = cpy.url;
            name = cpy.name;
            description = cpy.description;
            image = cpy.image;
            merchant = cpy.merchant;
            tax = cpy.tax;
            currency = cpy.currency;
            productPrice = cpy.productPrice;
            deliveryPrice = cpy.deliveryPrice;
            shippingExtra = cpy.shippingExtra;
        }
    }

    @Override
    public long getId() {
        return url.hashCode();
    }

    @Override
    public boolean isValid() {
        return !TextUtils.isEmpty(name) && deliveryPrice != NO_PRICE && productPrice != NO_PRICE && merchant != null;
    }

}
