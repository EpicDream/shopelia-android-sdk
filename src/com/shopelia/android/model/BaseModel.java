package com.shopelia.android.model;

import android.os.Parcelable;

public interface BaseModel<T> extends Parcelable, JsonData {

    public void merge(T item);

}
