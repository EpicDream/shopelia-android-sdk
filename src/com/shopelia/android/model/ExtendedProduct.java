package com.shopelia.android.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class ExtendedProduct implements JsonData {

    public interface Api extends Product.Api {
        String DOWNLOAD_TIME = "download_at";
        String JSON = "json";
        String READY = "ready";
    }

    public String url;
    public boolean ready;

    private JSONObject mJson;
    public long download_at;

    private Product mProduct;

    public ExtendedProduct() {

    }

    public ExtendedProduct(Product product) {
        url = product.url;
        mProduct = product;
    }

    public void setJson(JSONObject object) {
        mJson = object;
        ready = object.optInt(Api.READY, 0) == 1;
        mProduct = null;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(Api.URL, url);
        object.put(Api.DOWNLOAD_TIME, download_at);
        object.put(Api.JSON, mJson);
        return object;
    }

    public Product getProduct() {
        if (mProduct == null && mJson != null) {
            try {
                mProduct = Product.inflate(mJson);
                mProduct.url = !TextUtils.isEmpty(mProduct.url) ? mProduct.url : url;
            } catch (Exception e) {

            }
        }
        return mProduct;
    }

    public boolean isValid() {
        return getProduct() != null && getProduct().isValid();
    }

    public static ExtendedProduct inflate(JSONObject object) throws JSONException {
        ExtendedProduct product = new ExtendedProduct();
        product.url = object.getString(Api.URL);
        product.download_at = object.getLong(Api.DOWNLOAD_TIME);
        product.setJson(object.getJSONObject(Api.JSON));
        return product;
    }

    public static ArrayList<ExtendedProduct> inflate(JSONArray array) throws JSONException {
        final int count = array.length();
        ArrayList<ExtendedProduct> out = new ArrayList<ExtendedProduct>(count);
        for (int index = 0; index < count; index++) {
            out.add(ExtendedProduct.inflate(array.getJSONObject(index)));
        }
        return out;
    }
}
