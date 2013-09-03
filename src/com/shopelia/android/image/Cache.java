package com.shopelia.android.image;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.shopelia.android.model.JsonData;
import com.shopelia.android.utils.DigestUtils;
import com.shopelia.android.utils.IOUtils;

/**
 * Very simple cache management class. Kind of LRU cache management. Improvement
 * : Use Thread pool executor in order to execute on a worker thread I/O
 * operations
 * 
 * @author Pierre Pollastri
 */
class Cache {

    private static final String JOURNAL_FILENAME = ".journal";
    private static final long NEED_COMPUTATION = -1;

    private long mLifeExpectancy;
    private Journal mJournal;
    private File mCacheDir;
    private long mMaxSize;
    private long mSize = NEED_COMPUTATION;

    public Cache(File baseDir, long lifeExpectancy, long maxCacheSize) {
        mCacheDir = baseDir;
        mMaxSize = maxCacheSize;
        mLifeExpectancy = lifeExpectancy;
    }

    public Cache(Context context, String baseDirectory, long lifeExpectancy, long maxCacheSize) {
        this(new File(context.getFilesDir(), baseDirectory), lifeExpectancy, maxCacheSize);
        mLifeExpectancy = lifeExpectancy;
    }

    public synchronized File create(String filename) {
        filename = DigestUtils.md5(filename);
        ensureSafe();
        mSize = NEED_COMPUTATION;
        File file = new File(mCacheDir, mJournal.create(filename).filename);
        snapshot();
        return file;
    }

    public synchronized int getEntriesCount() {
        ensureSafe();
        return mJournal.size();
    }

    public synchronized long getSizeOnDisk() {
        ensureSafe();
        if (mSize == NEED_COMPUTATION) {
            mSize = computeSize();
        }
        return mSize;
    }

    public synchronized File load(String filename) {
        filename = DigestUtils.md5(filename);
        ensureSafe();
        mSize = NEED_COMPUTATION;
        if (mJournal.hasEntry(filename)) {
            File file = new File(mCacheDir, mJournal.get(filename).filename);
            snapshot();
            return file;
        }
        return null;
    }

    public synchronized boolean exists(String filename) {
        filename = DigestUtils.md5(filename);
        return mJournal.hasEntry(filename);
    }

    public synchronized void delete(String filename) {
        filename = DigestUtils.md5(filename);
        ensureSafe();
        mSize = NEED_COMPUTATION;
        if (mJournal.hasEntry(filename)) {
            new File(mCacheDir, filename).delete();
        }
        mJournal.delete(filename);
    }

    private long computeSize() {
        long size = 0;
        for (com.shopelia.android.image.Cache.Journal.Entry entry : mJournal) {
            File file = new File(mCacheDir, entry.filename);
            entry.size = file.length();
            size += file.length();
        }
        return size;
    }

    private void collect() {

        long size = computeSize();

        for (com.shopelia.android.image.Cache.Journal.Entry entry : mJournal) {

        }
    }

    public void clear() {
        if (mJournal != null) {
            mCacheDir.delete();
            mCacheDir.mkdirs();
            mJournal.clear();
            snapshot();
        }
    }

    private void snapshot() {
        try {
            File journal = new File(mCacheDir, JOURNAL_FILENAME);
            FileWriter writer = new FileWriter(journal);
            StringReader reader = new StringReader(mJournal.toJson().toString());
            IOUtils.copy(reader, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ensureSafe() {
        if (mCacheDir.exists() && !mCacheDir.isDirectory()) {
            mCacheDir.delete();
        }
        if (!mCacheDir.exists()) {
            mCacheDir.mkdirs();
        }
        if (mJournal == null) {
            File journal = new File(mCacheDir, JOURNAL_FILENAME);
            if (journal.exists()) {
                try {
                    StringWriter writer = new StringWriter();
                    FileReader reader = new FileReader(journal);
                    IOUtils.copy(reader, writer);
                    reader.close();
                    Log.d(null, "CACHE READING " + writer);
                    mJournal = Journal.inflate(new JSONObject(writer.toString()));

                } catch (Exception e) {
                    mJournal = new Journal();
                }
            } else {
                mJournal = new Journal();
            }
            collect();
        }
    }

    private static class Journal implements JsonData, Iterable<com.shopelia.android.image.Cache.Journal.Entry> {

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

        public Journal() {
            mCreatedAt = System.currentTimeMillis();
            mVersion = VERSION;
        }

        public boolean hasEntry(String filename) {
            return mEntries.containsKey(filename);
        }

        public Entry create(String filename) {
            Entry entry;
            if (mEntries.containsKey(filename)) {
                entry = mEntries.get(filename);
            } else {
                entry = new Entry(filename);
                mEntries.put(entry.filename, entry);
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
                entry.used++;
                entry.used_at = System.currentTimeMillis();
            }
            return entry;
        }

        public Map<String, Entry> getEntries() {
            return mEntries;
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

        public int size() {
            return mEntries.size();
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
            object.put(Api.ENTRIES, array);
            return object;
        }

        @Override
        public Iterator<com.shopelia.android.image.Cache.Journal.Entry> iterator() {
            return new It();
        }

        private class It implements Iterator<Entry> {

            private Iterator<Map.Entry<String, Entry>> it;

            public It() {
                it = mEntries.entrySet().iterator();
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Entry next() {
                return it.next().getValue();
            }

            @Override
            public void remove() {
                it.next();
            }

        }

    }

}
