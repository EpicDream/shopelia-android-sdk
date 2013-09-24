package com.shopelia.android.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.content.Context;

import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiController.OnApiErrorEvent;
import com.shopelia.android.remote.api.UserAPI;
import com.shopelia.android.remote.api.UserAPI.OnAccountCreationSucceedEvent;
import com.shopelia.android.remote.api.UserAPI.OnSignInEvent;
import com.shopelia.android.remote.api.UserAPI.OnUserRetrievedEvent;

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
        final UserAPI api = new UserAPI(context);
        api.register(new Object() {

            public void onEventMainThread(OnSignInEvent event) {
                result.value = event.resource;
                barrier.countDown();
            }

            public void onEventMainThread(OnApiErrorEvent event) {
                barrier.countDown();
            }

        });
        api.signIn(user);
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
        final UserAPI api = new UserAPI(context);
        api.register(new Object() {

            public void onEventMainThread(OnAccountCreationSucceedEvent event) {
                result.value = event.resource;
                barrier.countDown();
            }

            public void onEventMainThread(OnApiErrorEvent event) {
                barrier.countDown();
            }

        });
        api.createAccount(user, user.getDefaultAddress(), user.getDefaultPaymentCard());
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
        final UserAPI api = new UserAPI(context);
        api.register(new Object() {

            public void onEventMainThread(OnUserRetrievedEvent event) {
                result.value = event.resource;
                barrier.countDown();
            }

            public void onEventMainThread(OnApiErrorEvent event) {
                barrier.countDown();
            }

        });
        api.updateUser();
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
