package com.shopelia.android.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;

import android.test.InstrumentationTestCase;

import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiHandler.CallbackAdapter;
import com.shopelia.android.remote.api.UserAPI;
import com.shopelia.android.test.model.MockUser;
import com.turbomanage.httpclient.HttpResponse;

public class UserAPITest extends InstrumentationTestCase {

    public void testSignIn() {
        final CountDownLatch barrier = new CountDownLatch(1);
        final AtomicBoolean result = new AtomicBoolean(false);
        new UserAPI(getInstrumentation().getTargetContext(), new CallbackAdapter() {

            @Override
            public void onSignIn(User user) {
                result.set(true);
                barrier.countDown();
            }

            @Override
            public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
                barrier.countDown();
            }

        }).signIn(MockUser.get("me"));
        try {
            barrier.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(result.get());
    }

}
