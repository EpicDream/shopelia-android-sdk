package com.shopelia.android.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.InstrumentationTestCase;

import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiHandler.CallbackAdapter;
import com.shopelia.android.remote.api.VerifyAPI;
import com.shopelia.android.test.model.UserFactory;
import com.turbomanage.httpclient.HttpResponse;

public class VerifyAPITest extends InstrumentationTestCase {

    User user;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = UserFactory.get("me");
        TestUtils.signIn(getInstrumentation().getTargetContext(), user);
    }

    public void testVerifyUser() throws JSONException {
        assertTrue("Should be authentified",
                verifiy(new VerifyAPI(getInstrumentation().getTargetContext(), new CallbackAdapter()), user.password));
    }

    public void testBlockOrder() throws JSONException, InterruptedException {
        VerifyAPI api = new VerifyAPI(getInstrumentation().getTargetContext(), new CallbackAdapter());
        assertFalse("Should not be blocked", api.isOrderForbidden());
        for (int iteration = 0; iteration < 5; iteration++) {
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
        api.setCallback(new CallbackAdapter() {
            @Override
            public void onVerifySucceed() {
                super.onVerifySucceed();
                result.set(true);
                barrier.countDown();
            }

            @Override
            public void onVerifyFailed() {
                super.onVerifyFailed();
                barrier.countDown();
            }

            @Override
            public void onVerifyUpdateUI(VerifyAPI api, boolean locked, long delay, String message) {
                super.onVerifyUpdateUI(api, locked, delay, message);
                if (locked) {
                    barrier.countDown();
                }
            }

            @Override
            public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
                super.onError(step, httpResponse, response, e);
                barrier.countDown();
            }

        });
        api.verify(new JSONObject("{" + User.Api.PASSWORD + ": " + "\"" + password + "\"}"));

        try {
            barrier.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        return result.get();
    }
}
