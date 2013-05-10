package com.shopelia.android.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Utility class made to ease {@link Parcel} usage.
 * 
 * @author Pierre Pollastri
 */
public final class ParcelUtils {

    private ParcelUtils() {

    }

    public static void writeParcelable(Parcel dest, Parcelable parcelable, int flags) {
        if (dest == null) {
            return;
        }
        byte exists = (byte) (parcelable != null ? 1 : 0);
        dest.writeByte(exists);
        if (exists == 1) {
            dest.writeParcelable(parcelable, flags);
        }
    }

    public static <T> T readParcelable(Parcel source, ClassLoader classLoader) {
        if (source == null || classLoader == null) {
            return null;
        }
        if (source.readByte() == 1) {
            return source.readParcelable(classLoader);
        } else {
            return null;
        }
    }

}
