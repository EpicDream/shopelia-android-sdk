package com.shopelia.android.utils;

import java.util.List;

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

    public static <T extends Parcelable> T readParcelable(Parcel source, ClassLoader classLoader) {
        if (source == null) {
            return null;
        }
        if (source.readByte() == 1) {
            return source.readParcelable(classLoader);
        } else {
            return null;
        }
    }

    public static <T extends Parcelable> void writeParcelableList(Parcel dest, List<T> list, int flags) {
        if (dest == null) {
            return;
        }
        byte exists = (byte) (list != null ? 1 : 0);
        dest.writeByte(exists);
        if (exists == 1) {
            final int size = list.size();
            dest.writeInt(size);
            for (int index = 0; index < size; index++) {
                dest.writeParcelable(list.get(index), flags);
            }

        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Parcelable> List<T> readParcelableList(Parcel source, List<T> list, ClassLoader classLoader) {
        if (source == null) {
            return null;
        }
        if (source.readByte() == 1) {
            final int size = source.readInt();
            for (int index = 0; index < size; index++) {
                list.add((T) source.readParcelable(classLoader));
            }
        }
        return list;
    }

}
