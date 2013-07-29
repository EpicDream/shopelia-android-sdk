package com.shopelia.android.model;

import android.os.Parcelable;

public interface BaseModel<T> extends Parcelable, JsonData {

    public static final long NO_ID = -1;
    /**
     * Same thing as NO_ID but more logical denomination in some cases.
     */
    public static final long INVALID_ID = -1;

    public static final String NO_UUID = null;

    public void merge(T item);

    public long getId();

    public boolean isValid();

}
