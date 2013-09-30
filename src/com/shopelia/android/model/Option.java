package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

public class Option implements BaseModel<Option> {

    public interface Api {
        String TEXT = "text";
        String SRC = "src";
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
        return new JSONObject();
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void merge(Option item) {

    }

    public static Option[] inflateFromVersion(JSONObject object) {
        // TODO Auto-generated method stub
        return null;
    }

    public static long hashCode(Option... options) {
        return 0;
    }

}
