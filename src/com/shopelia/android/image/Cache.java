package com.shopelia.android.image;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.model.JsonData;

/**
 * Very simple cache management class. Kind of LRU cache management.n
 * 
 * @author Pierre Pollastri
 */
class Cache {

    private long mLifeExpectancy;
    private Journal mJournal;
    private File mCacheDir;
    private long mMaxSize;

    public Cache(Context context, String baseDirectory, long lifeExpectancy, long maxCacheSize) {
        mCacheDir = new File(context.getFilesDir(), baseDirectory);
        mMaxSize = maxCacheSize;
        mLifeExpectancy = lifeExpectancy;
    }

    public synchronized File create(String filename) {
        ensureSafe();
        return null;
    }

    public synchronized void save(String filename, Reader reader) {
        ensureSafe();
    }

    public synchronized File load(String filename) {
        ensureSafe();

        return null;
    }

    public synchronized boolean exists(String filename) {
        return mJournal.hasEntry(filename);
    }

    public synchronized void delete(String filename) {
        ensureSafe();
    }

    private void collect() {

    }

    public void clear() {
        mJournal.clear();
    }

    private void ensureSafe() {
        if (mCacheDir.exists() && !mCacheDir.isDirectory()) {
            mCacheDir.delete();
        }
        if (!mCacheDir.exists()) {
            mCacheDir.mkdirs();
        }
    }

    private static class Journal implements JsonData {

        public static final int VERSION = 0;

        interface Api {
            String VERSION = "version";
            String ENTRIES = "entries";
            String CREATED_AT = "created_at";
        }

        static class Entry implements JsonData {

            interface Api {
                String FILENAME = "filename";
                String CREATED_AT = "created_at";
                String USED_AT = "used_at";
                String SIZE = "size";
                String USED = "used";
            }

            String filename;
            long created_at;
            long used_at;
            long size;
            long used;

            @Override
            public JSONObject toJson() throws JSONException {
                JSONObject object = new JSONObject();
                object.put(Api.FILENAME, filename);
                object.put(Api.CREATED_AT, created_at);
                object.put(Api.USED_AT, used_at);
                object.put(Api.SIZE, size);
                object.put(Api.USED, used);
                return object;
            }

            public static Entry inflate(JSONObject object) {
                Entry entry = new Entry();
                entry.created_at = object.optLong(Api.CREATED_AT);
                entry.filename = object.optString(Api.FILENAME);
                entry.created_at = object.optLong(Api.CREATED_AT);
                entry.size = object.optLong(Api.SIZE);
                return entry;
            }

        }

        private int mVersion;
        private long mCreatedAt;
        private ArrayList<Entry> mEntries = new ArrayList<Cache.Journal.Entry>();

        public boolean hasEntry(String filename) {
            return false;
        }

        public void clear() {

        }

        @Override
        public JSONObject toJson() throws JSONException {
            JSONObject object = new JSONObject();
            object.put(Api.VERSION, VERSION);

            return object;
        }
    }

}
