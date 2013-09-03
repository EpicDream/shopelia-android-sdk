package com.shopelia.android.image;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.shopelia.android.utils.IOUtils;

public class CacheTest extends InstrumentationTestCase {

    Cache cache;
    FileModel file1;
    FileModel file2;
    FileModel bigFile;
    FileModel[] files;

    public Cache newCache() {
        return new Cache(getInstrumentation().getTargetContext(), "shopelia/cache", 5 * 1000, 512);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cache = newCache();
        cache.clear();
        file1 = new FileModel("file1", "Hello I am the first cached file");
        file2 = new FileModel("file2", "Hello I am the second cached file");
        files = new FileModel[20];
        for (int index = 0; index < files.length; index++) {
            files[index] = new FileModel("file#" + index, String.format("Hello I am the cached file number #%d", index));
        }
    }

    @SmallTest
    public void testSimpleCache() throws IOException {
        File test1 = cache.create(file1.name);
        file1.write(test1);
        assertTrue(cache.exists(file1.name));
        assertEquals(1, cache.getEntriesCount());
        cache = newCache();
        FileModel file = file1.derive();
        file.read(cache.load(file.name));
        assertEquals(file1.content, file.content);
    }

    public void testClearCache() throws IOException {
        int size = floodCache();
        cache.clear();
        assertEquals(0, cache.getEntriesCount());
        cache = newCache();
        assertEquals(0, cache.getEntriesCount());
    }

    public void testGetSizeOnDisk() throws IOException {
        int size = floodCache();
        assertTrue(size <= cache.getSizeOnDisk());
    }

    public void testCollect() throws IOException, InterruptedException {
        floodCache();
        useFiles(5, 10, 10, 100);
        cache.collect();
        FileModel f = files[9].derive();
        f.read(cache.load(f.name));
        assertEquals(f.source.content, f.content);
    }

    @Override
    protected void tearDown() throws Exception {
        cache.clear();
        super.tearDown();
    }

    private int floodCache() throws IOException {
        for (FileModel f : files) {
            File file = cache.create(f.name);
            f.write(file);
        }
        return files.length;
    }

    private void useFiles(int from, int to, int numberOfTimes, long delay) throws InterruptedException {
        for (int i = 0; i < numberOfTimes; i++) {
            for (; from < to; from++) {
                cache.load(files[from].name);
            }
            Thread.sleep(delay);
        }
    }

    private void useFile(int index, int numberOfTimes, long delay) {
        for (int i = 0; i < numberOfTimes; i++) {
            cache.load(files[index].name);
        }
    }

    class FileModel {
        final String name;
        String content;
        FileModel source;

        public FileModel(FileModel source) {
            this.name = source.name;
            this.source = source;
        }

        public FileModel(String name) {
            this.name = name;
        }

        public FileModel(String name, String content) {
            this.name = name;
            this.content = content;
        }

        public void write(File file) throws IOException {
            StringReader reader = new StringReader(content);
            FileWriter writer = new FileWriter(file);
            IOUtils.copy(reader, writer);
            writer.close();
        }

        public void read(File file) throws IOException {
            StringWriter writer = new StringWriter();
            FileReader reader = new FileReader(file);
            IOUtils.copy(reader, writer);
            content = writer.toString();
        }

        @Override
        public boolean equals(Object o) {
            return o.toString().equals(content);
        }

        public FileModel derive() {
            return new FileModel(this);
        }

        public boolean asSource() {
            return equals(source);
        }

    }

}
