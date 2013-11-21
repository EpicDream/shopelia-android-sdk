package com.shopelia.android.utils;

import java.math.BigDecimal;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;

import com.shopelia.android.model.Version;

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

    public static <T extends Parcelable> void writeLongSparseArray(Parcel dest, LongSparseArray<T> array, int flags) {
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
            dest.writeParcelable(array.valueAt(index), flags);
            ParcelUtils.writeParcelable(dest, array.valueAt(index), flags);
        }
    }

    public static <T extends Parcelable> LongSparseArray<T> readLongSparseArray(Parcel source, ClassLoader classLoader) {
        if (source.readByte() == 0) {
            return null;
        }
        final int size = source.readInt();
        final long[] keys = new long[size];
        source.readLongArray(keys);
        LongSparseArray<T> out = new LongSparseArray<T>(size);
        for (int index = 0; index < size; index++) {
            out.append(keys[index], (T) ParcelUtils.readParcelable(source, classLoader));
        }
        return out;
    }

    public static void writeVersionArray(Parcel dest, LongSparseArray<Version> array, int flags) {
        byte exists = (byte) (array != null ? 1 : 0);
        dest.writeByte(exists);
        if (array != null) {
            final int size = array.size();
            dest.writeInt(size);
            for (int index = 0; index < size; index++) {
                Version version = array.valueAt(index);
                if (version != null) {
                    dest.writeParcelable(version, flags);
                }
            }
        }
    }

    public static LongSparseArray<Version> readVersionsArray(Parcel source) {
        if (source.readByte() == 1) {
            LongSparseArray<Version> versions = new LongSparseArray<Version>();
            final int size = source.readInt();
            for (int index = 0; index < size; index++) {
                Version version = source.readParcelable(Version.class.getClassLoader());
                versions.append(version.getOptionHashcode(), version);
            }
            return versions;
        }
        return null;
    }

    public static void writeBigDecimal(Parcel dest, BigDecimal value, BigDecimal invalid) {
        byte exists = (byte) (value != null && !value.equals(invalid) ? 1 : 0);
        dest.writeByte(exists);
        if (exists == 1) {
            dest.writeSerializable(value);
        }
    }

    public static BigDecimal readBigDecimal(Parcel source, BigDecimal fallback) {
        if (source.readByte() == 1) {
            return (BigDecimal) source.readSerializable();
        }
        return fallback;
    }

    public static String[] readStringArray(Parcel source) {
        int size = source.readInt();
        String[] array = new String[size];
        source.readStringArray(array);
        return array;
    }

    public static void writeStringArray(Parcel dest, String[] array) {
        dest.writeInt(array != null ? array.length : 0);
        dest.writeStringArray(array);
    }

}
