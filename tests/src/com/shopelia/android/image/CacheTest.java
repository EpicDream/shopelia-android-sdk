package com.shopelia.android.image;

import java.io.File;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class CacheTest extends InstrumentationTestCase {

    Cache cache;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cache = new Cache(getInstrumentation().getTargetContext(), "shopelia/cache", 5 * 1000, 256);
    }

    @SmallTest
    public void testSimpleCache() {
        File test1 = cache.create("test1");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
