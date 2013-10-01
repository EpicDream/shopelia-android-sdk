package com.shopelia.android.model;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class Options extends ArrayList<Option> implements Parcelable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Options() {
        super();
    }

    private Options(Parcel source) {
        this(source.readArrayList(Options.class.getClassLoader()));
    }

    public Options(ArrayList<Option> options) {
        super();
        for (Option option : options) {
            add(option);
        }
    }

    @Override
    public boolean add(Option object) {
        if (contains(object)) {
            return false;
        }
        return super.add(object);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this);
    }

    public static final Creator<Options> CREATOR = new Creator<Options>() {

        @Override
        public Options[] newArray(int size) {
            return new Options[size];
        }

        @Override
        public Options createFromParcel(Parcel source) {
            return new Options(source);
        }
    };

}
