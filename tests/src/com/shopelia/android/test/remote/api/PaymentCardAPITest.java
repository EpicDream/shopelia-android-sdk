package com.shopelia.android.test.remote.api;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import android.test.InstrumentationTestCase;

import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiController.OnApiErrorEvent;
import com.shopelia.android.remote.api.PaymentCardAPI;
import com.shopelia.android.remote.api.PaymentCardAPI.OnAddPaymentCardEvent;
import com.shopelia.android.test.TestUtils;
import com.shopelia.android.test.model.PaymentCardFactory;
import com.shopelia.android.test.model.UserFactory;

public class PaymentCardAPITest extends InstrumentationTestCase {

    User user;
    PaymentCard paymentCard;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = TestUtils.signUp(getInstrumentation().getTargetContext(), UserFactory.get("test"));
        paymentCard = PaymentCardFactory.get("second");
    }

    public void testAddPaymentCard() {
        final CountDownLatch barrier = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);
        int before = user.paymentCards.size();

        PaymentCardAPI api = new PaymentCardAPI(getInstrumentation().getTargetContext());
        api.register(new Object() {

            public void onEventMainThread(OnAddPaymentCardEvent event) {
                result.set(true);
                barrier.countDown();
            }

            public void onEventMainThread(OnApiErrorEvent event) {
                barrier.countDown();
            }

        });
        api.addPaymentCard(paymentCard);

        try {
            barrier.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        assertTrue("Should returns true", result.get());
        User after = TestUtils.updateUser(getInstrumentation().getTargetContext());
        assertEquals("Should have on more payment card", before + 1, after.paymentCards.size());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
