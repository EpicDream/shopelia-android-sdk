package com.shopelia.android.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.shopelia.android.R;

/**
 * Helper enum including informations about Taxes
 * 
 * @author Pierre Pollastri
 */
public enum Tax implements Parcelable {
    ATI(R.string.shopelia_tax_ati), ET(R.string.shopelia_tax_et);

    private int mText;

    private Tax(int text) {
        mText = text;
    }

    public int getResId() {
        return mText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.toString());
    }

    public static final Parcelable.Creator<Tax> CREATOR = new Creator<Tax>() {

        @Override
        public Tax[] newArray(int size) {
            return new Tax[size];
        }

        @Override
        public Tax createFromParcel(Parcel source) {
            return Tax.valueOf(source.readString());
        }
    };
}
