package com.shopelia.android.utils;

import java.math.BigDecimal;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Helper enum including informations about currencies
 * 
 * @author Pierre Pollastri
 */
public enum Currency implements Parcelable {
    EUR("%d.%02d€");

    private String mFormat;

    private Currency(String format) {
        mFormat = format;
    }

    public String format(float value) {
        BigDecimal roundfinalPrice = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
        return roundfinalPrice.toPlainString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.toString());
    }

    public static final Parcelable.Creator<Currency> CREATOR = new Creator<Currency>() {

        @Override
        public Currency[] newArray(int size) {
            return new Currency[size];
        }

        @Override
        public Currency createFromParcel(Parcel source) {
            return Currency.valueOf(source.readString());
        }
    };
}
