package com.shopelia.android.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shopelia.android.model.JsonData;

/**
 * An advanced control able to handle multiple {@link HashSet}. Diff/Merge them
 * and have an internal history in order to cancel previous operations.
 * 
 * @author Pierre Pollastri
 * @param <K>
 * @param <V>
 */
public class MultiHashSet<K, V> extends HashMap<K, HashSet<V>> implements JsonData {

    public interface Revokator<E> {
        public boolean revoke(E item);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 2798560410237685233L;

    private HashMap<String, Stack<HashSet<V>>> mHistory = new HashMap<String, Stack<HashSet<V>>>();

    public HashSet<V> getSet(K setName) {
        if (!containsKey(setName)) {
            put(setName, new HashSet<V>());
        }
        return get(setName);
    }

    public boolean put(K setName, V value) {
        return getSet(setName).add(value);
    }

    public void revoke(K setName, Revokator<V> revokator) {
        HashSet<V> set = getSet(setName);
        Iterator<V> it = set.iterator();
        while (it.hasNext()) {
            V value = it.next();
            if (revokator.revoke(value)) {
                it.remove();
            }
        }
    }

    public HashSet<V> diff(K n1, K n2) {
        HashSet<V> diff = new HashSet<V>();
        HashSet<V> l1 = getSet(n1);
        HashSet<V> l2 = getSet(n2);
        for (V i1 : l1) {
            boolean found = false;
            for (V i2 : l2) {
                if (i2.equals(i1)) {
                    found = true;
                }
            }
            if (!found) {
                diff.add(i1);
            }
        }
        return diff;
    }

    public void merge(K from, K to) {
        HashSet<V> diff = diff(from, to);
        pushInHistory(from, to, diff);
        HashSet<V> dest = getSet(to);
        merge(diff, dest);
    }

    private void merge(HashSet<V> from, HashSet<V> to) {
        for (V value : from) {
            to.add(value);
        }
    }

    public void revert(K from, K to) {
        HashSet<V> diff = popFromHistory(from, to);
        if (diff != null) {
            merge(diff, getSet(from));
            HashSet<V> toSet = getSet(to);
            for (V value : diff) {
                toSet.remove(value);
            }
        }
    }

    public static <E> MultiHashSet<String, E> inflate(JSONObject object, JsonInflater<E> inflater) throws JSONException {
        MultiHashSet<String, E> out = new MultiHashSet<String, E>();
        JSONArray names = object.names();
        final int size = names.length();
        for (int index = 0; index < size; index++) {
            final String name = names.getString(index);
            JSONArray array = object.getJSONArray(name);
            final int s = array.length();
            HashSet<E> list = new HashSet<E>(s);
            for (int i = 0; i < s; i++) {
                list.add(inflater.inflate(array.getJSONObject(i)));
            }
            out.put(name, list);
        }
        return out;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        Set<Entry<K, HashSet<V>>> entries = entrySet();
        for (Entry<K, HashSet<V>> entry : entries) {
            HashSet<V> array = entry.getValue();
            JSONArray a = new JSONArray();
            for (V item : array) {
                if (item instanceof JsonData) {
                    JsonData jdata = (JsonData) item;
                    a.put(jdata.toJson());
                }
            }
            object.put(entry.getKey().toString(), a);
        }
        return object;
    }

    private void pushInHistory(K from, K to, HashSet<V> diff) {
        String entryName = from.toString() + "-" + to.toString();
        if (!mHistory.containsKey(entryName)) {
            mHistory.put(entryName, new Stack<HashSet<V>>());
        }
        mHistory.get(entryName).push(diff);
    }

    private HashSet<V> popFromHistory(K from, K to) {
        String entryName = from.toString() + "-" + to.toString();
        if (mHistory.containsKey(entryName) && mHistory.get(entryName).size() > 0) {
            return mHistory.get(entryName).pop();
        }
        return null;
    }

}
