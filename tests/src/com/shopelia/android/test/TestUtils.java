package com.shopelia.android.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiHandler.CallbackAdapter;
import com.shopelia.android.remote.api.UserAPI;
import com.shopelia.android.test.model.MockUser;
import com.turbomanage.httpclient.HttpResponse;

/**
 * Utils for every test operation
 * 
 * @author Pierre Pollastri
 */
public class TestUtils {

    public User signIn(Context context, CharSequence email, CharSequence password) {
        User user = new User();
        user.email = email.toString();
        user.password = password.toString();
        return signIn(context, user);
    }

    public User signIn(Context context, User user) {
        final CountDownLatch barrier = new CountDownLatch(1);
        final Mutable<User> result = new Mutable<User>();
        new UserAPI(context, new CallbackAdapter() {

            @Override
            public void onSignIn(User user) {
                result.value = user;
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

        }
        if (result.value == null) {
            throw new Error("Unable to login the user " + user.email + " " + user.password);
        }
        return result.value;
    }

    public static class Mutable<T> {
        public T value;

        public Mutable() {

        }

        public Mutable(T value) {
            this.value = value;
        }

    }

}