package com.shopelia.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.shopelia.android.R;

/**
 * Helper enum including informations about vendors
 * 
 * @author Pierre Pollastri
 */
public enum Vendor implements Parcelable {
    AMAZON("Amazon", R.drawable.amazon_logo);

    private String mName;
    private int mImageResId;

    private Vendor(String name, int resId) {
        mName = name;
        mImageResId = resId;
    }

    public String getName() {
        return mName;
    }

    public int getImageResId() {
        return mImageResId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.toString());
    }

    public static final Parcelable.Creator<Vendor> CREATOR = new Creator<Vendor>() {

        @Override
        public Vendor[] newArray(int size) {
            return new Vendor[size];
        }

        @Override
        public Vendor createFromParcel(Parcel source) {
            return Vendor.valueOf(source.readString());
        }
    };
}
