package com.shopelia.android.utils;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;

/**
 * Utility class made to ease {@link Parcel} usage.
 * 
 * @author Pierre Pollastri
 */
public final class ParcelUtils {

    public interface ArrayCreator<T> {
        public T[] newArray(int size);
    }

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

    public static void writeNullable(Parcel destination, Object object) {
        destination.writeByte((byte) (object != null ? 1 : 0));
        if (object != null) {
            destination.writeValue(destination);
        }
    }

    /**
     * @param source
     * @param loader may be null for default class loader
     * @return
     */
    public static <T> T readNullable(Parcel source, ClassLoader loader, T fallback) {
        boolean exists = source.readByte() == 1;
        if (exists) {
            return (T) source.readValue(loader);
        } else {
            return fallback;
        }
    }

    public static <T> void writeLongSparseArray(Parcel dest, LongSparseArray<T> array, int flags) {
        dest.writeByte((byte) (array != null ? 1 : 0));
        if (array == null) {
            return;
        }

        final int size = array.size();
        final long[] keys = new long[size];

        dest.writeInt(size);
        for (int index = 0; index < size; index++) {
            keys[index] = array.keyAt(index);
        }
        dest.writeLongArray(keys);
        for (int index = 0; index < size; index++) {
            dest.writeValue(array.valueAt(index));
        }
    }

    public static <T> LongSparseArray<T> readLongSparseArray(Parcel source, ClassLoader classLoader) {
        if (source.readByte() == 0) {
            return null;
        }
        final int size = source.readInt();
        final long[] keys = new long[size];
        source.readLongArray(keys);
        LongSparseArray<T> out = new LongSparseArray<T>();
        for (int index = 0; index < size; index++) {
            out.append(keys[index], (T) source.readValue(classLoader));
        }
        return out;
    }

}
