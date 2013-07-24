package com.shopelia.android.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shopelia.android.model.JsonData;

public class QualifiedLists<E> implements JsonData {

    private HashMap<String, ArrayList<E>> mContent = new HashMap<String, ArrayList<E>>();

    public QualifiedLists() {

    }

    public ArrayList<E> getList(String name) {
        if (mContent.containsKey(name)) {
            mContent.put(name, new ArrayList<E>());
        }
        return mContent.get(name);
    }

    public E get(String name, int position) {
        ArrayList<E> list = getList(name);
        return position < list.size() ? list.get(position) : null;
    }

    public void revoke(String name, Revokator<E> revokator) {
        ArrayList<E> list = getList(name);
        int size = list.size();
        for (int index = 0; index < size; index++) {
            if (revokator.revoke(list.get(index))) {
                index--;
                size--;
                list.remove(index);
            }
        }
    }

    public ArrayList<E> diff(String n1, String n2) {
        ArrayList<E> diff = new ArrayList<E>();
        ArrayList<E> l1 = getList(n1);
        ArrayList<E> l2 = getList(n2);
        for (E i1 : l1) {
            boolean found = false;
            for (E i2 : l2) {
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

    public void merge(String n1, String n2) {
        ArrayList<E> diff = diff(n1, n2);
        ArrayList<E> l2 = getList(n2);
        for (E item : diff) {
            l2.add(item);
        }
    }

    public static <E> QualifiedLists<E> inflate(JSONObject object, JsonInflater<E> inflater) throws JSONException {
        QualifiedLists<E> out = new QualifiedLists<E>();
        JSONArray names = object.names();
        final int size = names.length();
        for (int index = 0; index < size; index++) {
            final String name = names.getString(index);
            JSONArray array = object.getJSONArray(name);
            final int s = array.length();
            ArrayList<E> list = new ArrayList<E>(s);
            for (int i = 0; i < s; i++) {
                list.add(inflater.inflate(array.getJSONObject(index)));
            }
            out.mContent.put(name, list);
        }
        return out;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        Set<Entry<String, ArrayList<E>>> entries = mContent.entrySet();
        for (Entry<String, ArrayList<E>> entry : entries) {
            ArrayList<E> array = entry.getValue();
            JSONArray a = new JSONArray();
            for (E item : array) {
                if (item instanceof JsonData) {
                    JsonData jdata = (JsonData) item;
                    a.put(jdata);
                }
            }
            object.put(entry.getKey(), array);
        }
        return object;
    }

    public interface Revokator<E> {
        public boolean revoke(E item);
    }

}
