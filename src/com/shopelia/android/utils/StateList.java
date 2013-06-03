package com.shopelia.android.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A special list holding two sublist. You can add items in the list but there
 * are not accessible unless you commit you changes to the list.
 * 
 * @author Pierre Pollastri
 */
public class StateList<E> implements List<E> {

    private List<E> mStaged = new ArrayList<E>();
    private List<E> mCommitted = new ArrayList<E>();

    public StateList() {

    }

    public StateList(StateList<E> cpy) {
        for (E elem : cpy.mStaged) {
            mStaged.add(elem);
        }
        for (E elem : cpy.mCommitted) {
            mCommitted.add(elem);
        }
    }

    public void commit() {
        mCommitted.clear();
        for (E item : mStaged) {
            mCommitted.add(item);
        }
    }

    @Override
    public boolean add(E object) {
        return mStaged.add(object);
    }

    @Override
    public void add(int location, E object) {
        mStaged.add(location, object);
    }

    @Override
    public boolean addAll(Collection<? extends E> arg0) {
        return mStaged.addAll(arg0);
    }

    @Override
    public boolean addAll(int arg0, Collection<? extends E> arg1) {
        return mStaged.addAll(arg0, arg1);
    }

    @Override
    public void clear() {
        mStaged.clear();
    }

    @Override
    public boolean contains(Object object) {
        return mCommitted.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return mCommitted.containsAll(collection);
    }

    @Override
    public E get(int location) {
        return mCommitted.get(location);
    }

    @Override
    public int indexOf(Object object) {
        return mCommitted.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return mCommitted.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new ReadOnlyIterator<E>(mCommitted.iterator());
    }

    @Override
    public int lastIndexOf(Object object) {
        return mCommitted.lastIndexOf(object);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ReadOnlyListIterator<E>(mCommitted.listIterator());
    }

    @Override
    public ListIterator<E> listIterator(int location) {
        return new ReadOnlyListIterator<E>(mCommitted.listIterator(location));
    }

    @Override
    public E remove(int location) {
        return mStaged.remove(location);
    }

    @Override
    public boolean remove(Object object) {
        return mStaged.remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return mStaged.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return mStaged.retainAll(collection);
    }

    @Override
    public E set(int location, E object) {
        return mStaged.set(location, object);
    }

    @Override
    public int size() {
        return mCommitted.size();
    }

    @Override
    public List<E> subList(int start, int end) {
        return mCommitted.subList(start, end);
    }

    @Override
    public Object[] toArray() {
        return mCommitted.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return mCommitted.toArray(array);
    }

    private static class ReadOnlyIterator<E> implements Iterator<E> {

        private Iterator<E> mIterator;

        public ReadOnlyIterator(Iterator<E> it) {
            mIterator = it;
        }

        @Override
        public boolean hasNext() {
            return mIterator.hasNext();
        }

        @Override
        public E next() {
            return mIterator.next();
        }

        @Override
        public void remove() {

        }

    }

    private static class ReadOnlyListIterator<E> implements ListIterator<E> {

        private ListIterator<E> mIterator;

        public ReadOnlyListIterator(ListIterator<E> it) {
            mIterator = it;
        }

        @Override
        public void add(E object) {

        }

        @Override
        public boolean hasNext() {
            return mIterator.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return mIterator.hasPrevious();
        }

        @Override
        public E next() {
            return mIterator.next();
        }

        @Override
        public int nextIndex() {
            return mIterator.nextIndex();
        }

        @Override
        public E previous() {
            return mIterator.previous();
        }

        @Override
        public int previousIndex() {
            return mIterator.previousIndex();
        }

        @Override
        public void remove() {

        }

        @Override
        public void set(E object) {

        }

    }

}
