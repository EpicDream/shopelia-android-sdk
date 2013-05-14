package com.shopelia.android.api;

import java.util.Iterator;

import android.os.Parcel;
import android.os.Parcelable;

public class ShopeliaActivityPath implements Parcelable, Iterable<ShopeliaActivityPath> {

    public static final long INVALID_TIME = -1L;
    public static final int PARCEL_HEAD = 0x11EAD;
    public static final int PARCEL_OBJECT = 0x0B1EC7;
    public static final int PARCEL_STOP = 0xFEED;

    private String mActivityName;
    private long mCreationTime = INVALID_TIME;
    private long mDestroyTime = INVALID_TIME;
    private long mPauseTime = 0L;

    private long mPauseStartTime = INVALID_TIME;

    private ShopeliaActivityPath mPrevious;
    private ShopeliaActivityPath mNext;

    public ShopeliaActivityPath() {

    }

    public ShopeliaActivityPath(ShopeliaActivityPath src) {
        mActivityName = src.mActivityName;
        mCreationTime = src.mCreationTime;
        mDestroyTime = src.mDestroyTime;
    }

    private ShopeliaActivityPath(Parcel source) {
        this(source, source.readInt());
    }

    private ShopeliaActivityPath(Parcel source, int flags) {
        mActivityName = source.readString();
        mCreationTime = source.readLong();
        mDestroyTime = source.readLong();
        mPauseTime = source.readLong();
        mPauseStartTime = source.readLong();
        if (flags == PARCEL_HEAD) {
            while (source.readInt() == PARCEL_OBJECT) {
                ShopeliaActivityPath path = new ShopeliaActivityPath(source, PARCEL_OBJECT);
                path.mNext = first();
                first().mPrevious = path;
            }
            while (source.readInt() == PARCEL_OBJECT) {
                ShopeliaActivityPath path = new ShopeliaActivityPath(source, PARCEL_OBJECT);
                path.mPrevious = last();
                last().mNext = path;
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeSelfToParcel(dest, PARCEL_HEAD);
        ShopeliaActivityPath cursor = this.mPrevious;
        while (cursor != null) {
            cursor.writeSelfToParcel(dest, PARCEL_OBJECT);
            cursor = cursor.previous();
        }
        dest.writeInt(PARCEL_STOP);
        cursor = this.mNext;
        while (cursor != null) {
            cursor.writeSelfToParcel(dest, PARCEL_OBJECT);
            cursor = cursor.next();
        }
        dest.writeInt(PARCEL_STOP);
    }

    public void writeSelfToParcel(Parcel dest, int flags) {
        dest.writeInt(flags);
        dest.writeString(mActivityName);
        dest.writeLong(mCreationTime);
        dest.writeLong(mDestroyTime);
        dest.writeLong(mPauseTime);
        dest.writeLong(mPauseStartTime);
    }

    public boolean isFirst() {
        return mPrevious == null;
    }

    public boolean isLast() {
        return mNext == null;
    }

    public ShopeliaActivityPath previous() {
        return mPrevious;
    }

    public ShopeliaActivityPath next() {
        return mNext;
    }

    public ShopeliaActivityPath last() {
        ShopeliaActivityPath out = this;
        while (!out.isLast()) {
            out = out.next();
        }
        return out;
    }

    public ShopeliaActivityPath first() {
        ShopeliaActivityPath out = this;
        while (!out.isFirst()) {
            out = out.previous();
        }
        return out;
    }

    public ShopeliaActivityPath append(ShopeliaActivityPath path) {
        if (path == null) {
            return this;
        }
        path.last().append(mNext);
        path.mPrevious = this;
        mNext = path;
        return path;
    }

    public void setActivityName(String name) {
        mActivityName = name;
    }

    public String getActivityName() {
        return mActivityName;
    }

    public void startRecording() {
        if (mCreationTime == INVALID_TIME) {
            mCreationTime = System.currentTimeMillis();
        }
    }

    public void pause() {
        mPauseStartTime = System.currentTimeMillis();
    }

    public void resume() {
        if (mPauseStartTime != INVALID_TIME) {
            mPauseTime += (System.currentTimeMillis() - mPauseStartTime);
            mPauseStartTime = INVALID_TIME;
        }
    }

    public void stopRecording() {
        if (mDestroyTime == INVALID_TIME) {
            mDestroyTime = System.currentTimeMillis();
        }
    }

    public long getElapsedTime() {
        if (mDestroyTime == INVALID_TIME) {
            return INVALID_TIME;
        }
        return (mDestroyTime - mCreationTime) - mPauseTime;
    }

    @Override
    public Iterator<ShopeliaActivityPath> iterator() {
        return new PathIterator(this);
    }

    public Iterator<ShopeliaActivityPath> reverseIterator() {
        return new RevPathIterator(this);
    }

    private class PathIterator implements Iterator<ShopeliaActivityPath> {

        protected ShopeliaActivityPath mCurrentPath;

        public PathIterator(ShopeliaActivityPath currentPath) {
            mCurrentPath = currentPath;
        }

        @Override
        public boolean hasNext() {
            return !isLast();
        }

        @Override
        public ShopeliaActivityPath next() {
            mCurrentPath = mCurrentPath.next();
            return mCurrentPath;
        }

        @Override
        public void remove() {

        }

    }

    private class RevPathIterator extends PathIterator {

        public RevPathIterator(ShopeliaActivityPath currentPath) {
            super(currentPath);
        }

        @Override
        public boolean hasNext() {
            return !mCurrentPath.isFirst();
        }

        @Override
        public ShopeliaActivityPath next() {
            mCurrentPath = mCurrentPath.previous();
            return mCurrentPath;
        }

    }

    public static final Parcelable.Creator<ShopeliaActivityPath> CREATOR = new Creator<ShopeliaActivityPath>() {

        @Override
        public ShopeliaActivityPath[] newArray(int size) {
            return new ShopeliaActivityPath[size];
        }

        @Override
        public ShopeliaActivityPath createFromParcel(Parcel source) {
            return new ShopeliaActivityPath(source);
        }
    };

}
