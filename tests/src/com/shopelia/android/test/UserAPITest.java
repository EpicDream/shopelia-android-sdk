package com.shopelia.android.test;

import android.test.InstrumentationTestCase;

import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.test.model.MockUser;

public class UserAPITest extends InstrumentationTestCase {

    public void testSignIn() {
        User me = MockUser.get("me");
        TestUtils.signIn(getInstrumentation().getTargetContext(), me);
        assertEquals("Should have email = " + me.email, UserManager.get(getInstrumentation().getTargetContext()).getUser().email, me.email);
        assertTrue("Should be signed in", UserManager.get(getInstrumentation().getTargetContext()).isLogged());
    }

    public void testSignOut() {
        testSignIn();
        TestUtils.signOut(getInstrumentation().getTargetContext());
        assertFalse("Should not be logged ", UserManager.get(getInstrumentation().getTargetContext()).isLogged());
    }
}
