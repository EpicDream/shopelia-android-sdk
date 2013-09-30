package com.shopelia.android.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.support.v4.util.LongSparseArray;

public class Versions implements BaseModel<Versions> {

    private LongSparseArray<Version> mVersions = new LongSparseArray<Version>();
    private Options[] mOptions;
    private long mFirstKey = 0;
    private boolean mIsValid = false;

    public Versions() {

    }

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
        final int size = source.length();
        Versions versions = new Versions();
        versions.mIsValid = true;
        for (int index = 0; index < size; index++) {
            JSONObject object = source.getJSONObject(index);
            Version version = Version.inflate(object);
            Option[] options = Option.inflateFromVersion(object);
            long key = Option.hashCode(options);
            if (index == 0) {
                versions.mFirstKey = key;
            }
            version.setOptions(key);
            versions.mVersions.append(key, version);
            versions.appendOptions(options);
            versions.mIsValid = versions.mIsValid && version.isValid();
        }
        return versions;
    }

    private void appendOptions(Option... options) {
        if (mOptions == null) {
            mOptions = new Options[options.length];
            for (int index = 0; index < mOptions.length; index++) {
                mOptions[index] = new Options();
            }
        }
        final int size = options.length;
        for (int index = 0; index < size; index++) {
            mOptions[index].add(options[index]);
        }
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
        return mIsValid;
    }

    public long getFirstKey() {
        return mFirstKey;
    }

}
