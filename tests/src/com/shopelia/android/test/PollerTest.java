package com.shopelia.android.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.shopelia.android.http.AbstractPoller;
import com.shopelia.android.http.AbstractPoller.OnPollerEventListener;

public class PollerTest extends InstrumentationTestCase {

    TestPoller poller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final CountDownLatch barrier = new CountDownLatch(1);
        getInstrumentation().runOnMainSync(new Runnable() {

            @Override
            public void run() {
                poller = new TestPoller();
                poller.setExpiryDuration(1000);
                poller.setRequestFrequency(10);
                barrier.countDown();
            }
        });
        barrier.await();
    }

    public void testShouldSucceedAfter3() {
        final CountDownLatch barrier = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);
        poller.setOnPollerEventListener(new OnPollerEventListener<Integer>() {

            @Override
            public void onTimeExpired() {
                barrier.countDown();
            }

            @Override
            public boolean onResult(Integer previousResult, Integer newResult) {
                return newResult.intValue() > 3;
            }

            @Override
            public void onPollingSucceed() {
                result.set(true);
                barrier.countDown();
            }

        });
        poller.poll();
        try {
            barrier.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        assertTrue("Should be a success", result.get());
    }

    public void testShouldExpire() {
        final CountDownLatch barrier = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(true);
        poller.setOnPollerEventListener(new OnPollerEventListener<Integer>() {

            @Override
            public void onTimeExpired() {
                result.set(false);
                barrier.countDown();
            }

            @Override
            public boolean onResult(Integer previousResult, Integer newResult) {
                return false;
            }

            @Override
            public void onPollingSucceed() {
                barrier.countDown();
            }

        });
        poller.poll();
        try {
            barrier.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        assertFalse("Should be a failure", result.get());
    }

    private static class TestPoller extends AbstractPoller<Void, Integer> {

        private int count;

        public TestPoller() {
            super("TestPollerOnTheWay");
        }

        @Override
        protected void onStart() {
            super.onStart();
            count = 0;
        }

        @Override
        protected Integer execute(Void param) {
            Log.d(null, "COUNT = " + count);
            return new Integer(count++);
        }

    }

    @Override
    protected void tearDown() throws Exception {
        poller.stop();
        poller = null;
        super.tearDown();
    }

}
