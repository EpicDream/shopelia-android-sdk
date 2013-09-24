package com.shopelia.android.test.remote.api;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;

import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiController.OnApiErrorEvent;
import com.shopelia.android.remote.api.VerifyAPI;
import com.shopelia.android.remote.api.VerifyAPI.OnUpdateUiEvent;
import com.shopelia.android.remote.api.VerifyAPI.OnVerifyFailedEvent;
import com.shopelia.android.remote.api.VerifyAPI.OnVerifySucceedEvent;
import com.shopelia.android.test.TestUtils;
import com.shopelia.android.test.model.UserFactory;

public class VerifyAPITest extends InstrumentationTestCase {

    User user;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = UserFactory.get("me");
        TestUtils.signIn(getInstrumentation().getTargetContext(), user);
    }

    public void testVerifyUser() throws JSONException {
        assertTrue("Should be authentified", verifiy(new VerifyAPI(getInstrumentation().getTargetContext()), user.password));
    }

    public void testBlockOrder() throws JSONException, InterruptedException {
        VerifyAPI api = new VerifyAPI(getInstrumentation().getTargetContext());
        assertFalse("Should not be blocked", api.isOrderForbidden());
        for (int iteration = 0; iteration < 10; iteration++) {
            verifiy(api, "wrong" + user.password);
        }
        assertTrue("Should be blocked", api.isOrderForbidden());
        if (api.getUnlockDelay() > 0) {
            Thread.sleep(api.getUnlockDelay());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        UserManager.get(getInstrumentation().getTargetContext()).logout();
        super.tearDown();
    }

    private boolean verifiy(VerifyAPI api, String password) throws JSONException {
        final CountDownLatch barrier = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);
        api.register(new Object() {

            public void onEventMainThread(OnVerifySucceedEvent event) {
                result.set(true);
                barrier.countDown();
            }

            public void onEventMainThread(OnVerifyFailedEvent event) {
                barrier.countDown();
            }

            public void onEventMainThread(OnUpdateUiEvent event) {
                if (event.shouldBlock) {
                    barrier.countDown();
                }
            }

            public void onEventMainThread(OnApiErrorEvent event) {
                barrier.countDown();
            }

        });

        api.verify(new JSONObject("{" + User.Api.PASSWORD + ": " + "\"" + password + "\"}"));

        try {
            barrier.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        return result.get();
    }
}
