package com.shopelia.android.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.util.SparseArray;

public class Versions implements BaseModel<Versions> {

    private SparseArray<Product> mVersions;

    public int getOptionsCount() {
        return 0;
    }

    public ArrayList<Option> getOptions(int index) {
        return null;
    }

    public Product getProduct(Option... options) {
        return null;
    }

    public static Versions inflate(JSONArray source) throws JSONException {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @Override
    public JSONObject toJson() throws JSONException {
        return null;
    }

    @Override
    public void merge(Versions item) {

    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public long getFirst() {
        return 0;
    }

}
