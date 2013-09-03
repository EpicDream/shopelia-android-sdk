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
        return new Cache(getInstrumentation().getTargetContext(), "shopelia/cache", 5 * 1000, 256);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cache = newCache();
        file1 = new FileModel("file1", "Hello I am the first cached file");
        file2 = new FileModel("file2", "Hello I am the second cached file");
        files = new FileModel[20];
        for (int index = 0; index < files.length; index++) {
            files[index] = new FileModel("file#" + index, String.format("Hello I am the cached file number #$1", index));
        }
    }

    @SmallTest
    public void testSimpleCache() throws IOException {
        File test1 = cache.create(file1.name);
        file1.write(test1);
        cache = newCache();
        FileModel file = file1.derive();
        file.read(cache.load(file.name));
        assertEquals(file1.content, file.content);
    }

    @Override
    protected void tearDown() throws Exception {
        cache.clear();
        super.tearDown();
    }

    class FileModel {
        final String name;
        String content;

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
            return new FileModel(name);
        }

    }

}
