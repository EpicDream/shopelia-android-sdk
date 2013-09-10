package com.shopelia.android.app.tracking;

import java.util.concurrent.CountDownLatch;

import android.test.InstrumentationTestCase;

import com.shopelia.android.app.tracking.UUIDManager.OnReceiveUuidListener;
import com.shopelia.android.test.TestUtils.Mutable;

public class UUIDManagerTest extends InstrumentationTestCase {

    public void testUuidIsTheSame() {
        final CountDownLatch barrier = new CountDownLatch(1);
        final CountDownLatch secondBarrier = new CountDownLatch(1);
        final Mutable<String> expected = new Mutable<String>();
        final Mutable<String> result = new Mutable<String>();

        UUIDManager.obtainUuid(new OnReceiveUuidListener() {

            @Override
            public void onReceiveUuid(String uuid) {
                expected.value = uuid;
                barrier.countDown();
            }
        });
        try {
            barrier.await();
        } catch (InterruptedException e) {

        }
        UUIDManager.release();

        UUIDManager.obtainUuid(new OnReceiveUuidListener() {

            @Override
            public void onReceiveUuid(String uuid) {
                result.value = uuid;
                secondBarrier.countDown();
            }
        });
        try {
            secondBarrier.await();
        } catch (InterruptedException e) {

        }
        assertEquals(expected.value, result.value);
    }

}
