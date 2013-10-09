package com.shopelia.android.model;

import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;

import com.shopelia.android.utils.ParcelUtils;

public class Versions implements BaseModel<Versions> {

    private LongSparseArray<Version> mVersions = new LongSparseArray<Version>();
    private Options[] mOptions;
    private long mFirstKey = 0;
    private boolean mIsValid = false;

    public Versions() {

    }

    private Versions(Parcel source) {
        mVersions = ParcelUtils.readVersionsArray(source);
        Parcelable[] p = source.readParcelableArray(Option.class.getClassLoader());
        mOptions = new Options[p.length];
        System.arraycopy(p, 0, mOptions, 0, p.length);
        mFirstKey = source.readLong();
        mIsValid = source.readByte() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeVersionArray(dest, mVersions, flags);
        dest.writeParcelableArray(mOptions, flags);
        dest.writeLong(mFirstKey);
        dest.writeByte((byte) (mIsValid ? 1 : 0));
    }

    public void addVersion(Version version) {
        mVersions.append(version.getOptionHashcode(), version);
    }

    public int getOptionsCount() {
        return mOptions != null ? mOptions.length : 0;
    }

    public Options getOptions(int index) {
        return mOptions[index];
    }

    public Version getVersion(Option... options) {
        return getVersion(Option.hashCode(options));
    }

    public Version getVersion(long key) {
        return mVersions.get(key);
    }

    public boolean hasVersion(Option... options) {
        return hasVersion(Option.hashCode(options));
    }

    public boolean hasVersion(long key) {
        return getVersion(key) != null;
    }

    public int getVersionsCount() {
        return mVersions.size();
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
            version.setOptions(key, options);
            versions.mVersions.append(key, version);
            versions.appendOptions(options);
            versions.mIsValid = versions.mIsValid && version.isValid();
        }
        for (Options options : versions.mOptions) {
            Collections.sort(options, new Comparator<Option>() {

                @Override
                public int compare(Option lhs, Option rhs) {
                    return lhs.getValue().compareToIgnoreCase(rhs.getValue());
                }

            });
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

    public static final Parcelable.Creator<Versions> CREATOR = new Creator<Versions>() {

        @Override
        public Versions[] newArray(int size) {
            return new Versions[size];
        }

        @Override
        public Versions createFromParcel(Parcel source) {
            return new Versions(source);
        }
    };

}
