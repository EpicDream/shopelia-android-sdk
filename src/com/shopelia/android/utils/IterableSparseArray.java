package com.shopelia.android.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import android.util.SparseArray;

public class IterableSparseArray<E> extends SparseArray<E> implements Iterable<E> {

    public IterableSparseArray() {
        super();
    }

    public IterableSparseArray(int capacity) {
        super(capacity);
    }

    @Override
    public Iterator<E> iterator() {
        return new PrivateIterator();
    }

    private class PrivateIterator implements Iterator<E> {

        private int mIndex = -1;
        private int mCount;

        public PrivateIterator() {
            mCount = size();
        }

        @Override
        public boolean hasNext() {
            return mIndex < mCount;
        }

        @Override
        public E next() {
            if (mCount != size()) {
                throw new IllegalAccessError();
            }
            return valueAt(++mIndex);
        }

        @Override
        public void remove() {
            if (mIndex == -1 || mIndex >= mCount) {
                throw new NoSuchElementException();
            }
            removeAt(mIndex);
            mIndex -= 1;
            mCount -= 1;
        }
    }

}
