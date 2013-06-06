package com.shopelia.android.api;

import android.os.Parcel;
import android.os.Parcelable;

public class ShopeliaActivityResult implements Parcelable {

    private int mResult;

    public ShopeliaActivityResult(int result) {
        mResult = result;
    }

    private ShopeliaActivityResult(Parcel source) {
        mResult = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mResult);
    }

    public static final Parcelable.Creator<ShopeliaActivityResult> CREATOR = new Creator<ShopeliaActivityResult>() {

        @Override
        public ShopeliaActivityResult[] newArray(int size) {
            return new ShopeliaActivityResult[size];
        }

        @Override
        public ShopeliaActivityResult createFromParcel(Parcel source) {
            return new ShopeliaActivityResult(source);
        }
    };

}
