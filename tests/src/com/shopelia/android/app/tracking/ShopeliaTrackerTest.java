package com.shopelia.android.app.tracking;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.shopelia.android.app.tracking.ShopeliaTracker.FlushDelegate;
import com.shopelia.android.test.TestUtils.Mutable;
import com.shopelia.android.utils.TimeUnits;

public class ShopeliaTrackerTest extends InstrumentationTestCase {

    public static final String URL = "http://my_fake_web_site.fk/product_";
    ShopeliaTracker tracker;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tracker = (ShopeliaTracker) Tracker.Factory.getTracker(Tracker.PROVIDER_SHOPELIA, getInstrumentation().getTargetContext());
        tracker.setExpiryDelay(5 * TimeUnits.SECONDS);
    }

    @SmallTest
    public void testSimpleEvent() throws InterruptedException {
        final long now = System.currentTimeMillis();
        final Mutable<Long> onFlushTime = new Mutable<Long>();
        final Mutable<String> action = new Mutable<String>();
        final Mutable<String> trackerName = new Mutable<String>();
        final Mutable<String> result = new Mutable<String>();
        final CountDownLatch barrier = new CountDownLatch(1);

        tracker.setFlushDelegate(new FlushDelegate() {

            @Override
            public boolean send(String uuid, String t, String a, ArrayList<ShopeliaEvent> events) {
                onFlushTime.value = System.currentTimeMillis();
                action.value = a;
                trackerName.value = t;
                result.value = events.get(0).url;
                barrier.countDown();
                return false;
            }
        });
        tracker.onDisplayShopeliaButton(URL, "Test");
        barrier.await();
        assertTrue("Should wait at least 2 seconds before flushing instead of " + ((onFlushTime.value - now) / 1000), onFlushTime.value
                - now >= 2 * TimeUnits.SECONDS);
        assertEquals("Test", trackerName.value);
        assertEquals(URL, result.value);
    }

    @MediumTest
    public void testMultipleEvents() throws InterruptedException {
        final long now = System.currentTimeMillis();
        final Mutable<Long> onFlushTime = new Mutable<Long>();
        final Mutable<String> action = new Mutable<String>();
        final Mutable<String> trackerName = new Mutable<String>();
        final Mutable<Integer> result = new Mutable<Integer>();
        final CountDownLatch barrier = new CountDownLatch(1);

        tracker.setFlushDelegate(new FlushDelegate() {

            int iteration = 0;

            @Override
            public boolean send(String uuid, String t, String a, ArrayList<ShopeliaEvent> events) {
                assertTrue("Should be flushed once", iteration++ == 0);
                onFlushTime.value = System.currentTimeMillis();
                action.value = a;
                trackerName.value = t;
                result.value = events.size();
                barrier.countDown();
                return false;
            }
        });
        trackDisplay(0, 20, "Test");
        barrier.await();
        assertTrue("Should wait at least 2 seconds before flushing instead of " + ((onFlushTime.value - now) / 1000), onFlushTime.value
                - now >= 2 * TimeUnits.SECONDS);
        assertEquals("Test", trackerName.value);
        assertTrue(result.value.intValue() >= 19);
    }

    public void trackDisplay(int from, int to, String trackerName) {
        for (; from < to; from++) {
            tracker.onDisplayShopeliaButton(URL + from, trackerName);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        final CountDownLatch barrier = new CountDownLatch(1);
        tracker.setFlushDelegate(new FlushDelegate() {

            @Override
            public boolean send(String uuid, String tracker, String action, ArrayList<ShopeliaEvent> events) {
                barrier.countDown();
                return true;
            }
        });
        tracker.flush();
        barrier.await(3, java.util.concurrent.TimeUnit.SECONDS);
        super.tearDown();
    }

}
