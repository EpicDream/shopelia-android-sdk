package com.shopelia.android.app.tracking;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import android.test.InstrumentationTestCase;
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
                return true;
            }
        });
        tracker.onDisplayShopeliaButton(URL, "Test");
        barrier.await();
        assertTrue("Should wait at least 2 seconds before flushing", onFlushTime.value - now >= 2 * TimeUnits.SECONDS);
        assertEquals("Test", trackerName.value);
        assertEquals(URL, result.value);
    }
}
