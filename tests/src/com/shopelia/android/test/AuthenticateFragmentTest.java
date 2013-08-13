package com.shopelia.android.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;
import com.shopelia.android.WelcomeActivity;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.test.fixtures.FixtureBundleFactory;
import com.shopelia.android.test.model.UserFactory;

public class AuthenticateFragmentTest extends ActivityInstrumentationTestCase2<WelcomeActivity> {

    Solo solo;
    User user;

    @SuppressLint("NewApi")
    public AuthenticateFragmentTest() {
        super(WelcomeActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(FixtureBundleFactory.getShopeliaIntentBundle("first"));
        setActivityIntent(intent);
        solo = new Solo(getInstrumentation(), getActivity());
        user = UserFactory.get("me");
        TestUtils.signIn(getInstrumentation().getTargetContext(), user);
    }

    public void testLogout() {
        getActivity().getShopeliaActionBar().clickOnItem(R.id.shopelia_action_bar_sign_out);
        solo.waitForText(getActivity().getString(R.string.shopelia_logout_are_you_sure), 1, 1000);
        solo.clickOnText(getActivity().getString(R.string.shopelia_logout_positive));
        solo.waitForDialogToClose(1000);
        assertFalse("Should be sign out", UserManager.get(getActivity()).isLogged());
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

}
