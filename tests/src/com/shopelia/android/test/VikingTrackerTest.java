package com.shopelia.android.test;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.shopelia.android.app.tracking.VikingTracker;
import com.shopelia.android.app.tracking.VikingTracker.Entry;
import com.shopelia.android.app.tracking.VikingTracker.FlushDelegate;
import com.shopelia.android.test.TestUtils.Mutable;

public class VikingTrackerTest extends InstrumentationTestCase {

    public static final String URL = "http://my_fake_web_site.fk/product_";

    VikingTracker tracker;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final CountDownLatch barrier = new CountDownLatch(1);
        getInstrumentation().runOnMainSync(new Runnable() {

            @Override
            public void run() {
                tracker = VikingTracker.getInstance();
                barrier.countDown();
            }
        });
        try {
            barrier.await();
        } catch (InterruptedException e) {

        }
    }

    @SmallTest
    public void testSimpleEvent() {
        final CountDownLatch barrier = new CountDownLatch(1);
        final Mutable<HashSet<Entry>> result = new Mutable<HashSet<Entry>>();
        tracker.setFlushDelegate(new FlushDelegate() {

            @Override
            public void onFlush(HashSet<Entry> entries) {
                result.value = entries;
                barrier.countDown();
            }
        });
        tracker.onDisplayShopeliaButton(URL, "SuperTracker");
        try {
            barrier.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        assertTrue(result.value != null && result.value.size() == 1);
        assertEquals(URL, result.value.iterator().next().url);
    }
}
