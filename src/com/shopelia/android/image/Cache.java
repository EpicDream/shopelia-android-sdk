package com.shopelia.android.image;

import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
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

            public static final long NOT_COMPUTED = -1;
            public static final long NO_FILE = 0;

            String filename;
            long created_at;
            long used_at;
            long size;
            long used;

            public Entry() {

            }

            public Entry(String filename) {
                this.filename = filename;
                used = 0;
                size = NOT_COMPUTED;
            }

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
        private HashMap<String, Entry> mEntries = new HashMap<String, Cache.Journal.Entry>();

        public boolean hasEntry(String filename) {
            return false;
        }

        public Entry create(String filename) {
            Entry entry;
            if (mEntries.containsKey(filename)) {
                entry = mEntries.get(filename);
            } else {
                entry = new Entry(filename);
            }
            entry.used = 1;
            entry.created_at = System.currentTimeMillis();
            entry.used_at = System.currentTimeMillis();
            entry.size = Entry.NOT_COMPUTED;
            return entry;
        }

        public Entry get(String filename) {
            Entry entry = null;
            if (mEntries.containsKey(filename)) {
                entry = mEntries.get(filename);
            }
            return entry;
        }

        public void delete(String filename) {
            if (mEntries.containsKey(filename)) {
                Entry entry = mEntries.get(filename);
                if (entry.size == Entry.NO_FILE) {
                    mEntries.remove(entry);
                } else {
                    entry.size = Entry.NO_FILE;
                }
            }
        }

        public void clear() {
            mEntries.clear();
        }

        public static Journal inflate(JSONObject object) {
            Journal journal = new Journal();
            journal.mCreatedAt = object.optLong(Api.CREATED_AT);
            journal.mVersion = object.optInt(Api.VERSION);
            JSONArray array = object.optJSONArray(Api.ENTRIES);
            final int size = array.length();
            journal.mEntries = new HashMap<String, Cache.Journal.Entry>(array.length());
            for (int index = 0; index < size; index++) {
                try {
                    Entry entry = Entry.inflate(array.getJSONObject(index));
                    journal.mEntries.put(entry.filename, entry);
                } catch (JSONException e) {

                }
            }
            if (journal.mVersion != VERSION) {
                journal.migrate(journal.mVersion);
            }
            return journal;
        }

        private void migrate(int from) {

        }

        @Override
        public JSONObject toJson() throws JSONException {
            JSONObject object = new JSONObject();
            object.put(Api.VERSION, VERSION);
            object.put(Api.CREATED_AT, mCreatedAt);
            JSONArray array = new JSONArray();
            Set<Map.Entry<String, Entry>> entries = mEntries.entrySet();
            for (Map.Entry<String, Entry> e : entries) {
                if (e.getValue() != null) {
                    array.put(e.getValue().toJson());
                }
            }
            return object;
        }
    }

}
