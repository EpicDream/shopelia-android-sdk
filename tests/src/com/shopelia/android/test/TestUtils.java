package com.shopelia.android.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiController.CallbackAdapter;
import com.shopelia.android.remote.api.UserAPI;
import com.turbomanage.httpclient.HttpResponse;

/**
 * Utils for every test operation
 * 
 * @author Pierre Pollastri
 */
public class TestUtils {

    public static User signIn(Context context, CharSequence email, CharSequence password) {
        User user = new User();
        user.email = email.toString();
        user.password = password.toString();
        return signIn(context, user);
    }

    public static User signIn(Context context, User user) {
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

        }).signIn(user);
        try {
            barrier.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        if (result.value == null) {
            throw new Error("Unable to login the user " + user.email + " " + user.password);
        }
        return result.value;
    }

    public static User signUp(Context context, User user) {
        final CountDownLatch barrier = new CountDownLatch(1);
        final Mutable<User> result = new Mutable<User>();
        new UserAPI(context, new CallbackAdapter() {

            @Override
            public void onAccountCreationSucceed(User user, Address address) {
                result.value = user;
                barrier.countDown();
            }

            @Override
            public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
                barrier.countDown();
            }

        }).createAccount(user, user.getDefaultAddress(), user.getDefaultPaymentCard());
        try {
            barrier.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        if (result.value == null) {
            throw new Error("Unable to create user " + user.email);
        }
        return result.value;
    }

    /**
     * Must be sign in else will fail
     * 
     * @param context
     * @return
     */
    public static User updateUser(Context context) {
        final CountDownLatch barrier = new CountDownLatch(1);
        final Mutable<User> result = new Mutable<User>();
        new UserAPI(context, new CallbackAdapter() {

            @Override
            public void onUserRetrieved(User user) {
                result.value = user;
            }

            @Override
            public void onUserUpdateDone() {
                barrier.countDown();
            }

        }).updateUser();
        try {
            barrier.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        if (result.value == null) {
            throw new Error("Unable to update the user (perhaps you are not logged in)");
        }
        return result.value;
    }

    public static void signOut(Context context) {
        UserManager.get(context).logout();
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
