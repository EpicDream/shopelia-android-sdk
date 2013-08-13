package com.shopelia.android.test.remote.api;

import android.test.InstrumentationTestCase;

import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.test.TestUtils;
import com.shopelia.android.test.model.UserFactory;

public class UserAPITest extends InstrumentationTestCase {

    public void testSignIn() {
        User me = UserFactory.get("me");
        TestUtils.signIn(getInstrumentation().getTargetContext(), me);
        assertEquals("Should have email = " + me.email, UserManager.get(getInstrumentation().getTargetContext()).getUser().email, me.email);
        assertTrue("Should be signed in", UserManager.get(getInstrumentation().getTargetContext()).isLogged());
    }

    public void testSignOut() {
        testSignIn();
        TestUtils.signOut(getInstrumentation().getTargetContext());
        assertFalse("Should not be logged ", UserManager.get(getInstrumentation().getTargetContext()).isLogged());
    }

    public void testCreateUser() {
        User user = UserFactory.get("test");
        User result = TestUtils.signUp(getInstrumentation().getTargetContext(), user);
        assertEquals("Should have email = " + user.email, UserManager.get(getInstrumentation().getTargetContext()).getUser().email,
                user.email);
        assertTrue("Should be signed in", UserManager.get(getInstrumentation().getTargetContext()).isLogged());
        assertEquals("Should have the same address", user.getDefaultAddress().address, result.getDefaultAddress().address);
    }

}
