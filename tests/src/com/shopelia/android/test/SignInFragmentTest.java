package com.shopelia.android.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;
import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.model.User;
import com.shopelia.android.test.fixtures.FixtureBundleFactory;
import com.shopelia.android.test.model.UserFactory;

public class SignInFragmentTest extends ActivityInstrumentationTestCase2<PrepareOrderActivity> {

    Solo solo;
    User user;

    @SuppressLint("NewApi")
    public SignInFragmentTest() {
        super(PrepareOrderActivity.class);
        user = UserFactory.get("me");
        TestUtils.signIn(getInstrumentation().getTargetContext(), user);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(FixtureBundleFactory.getShopeliaIntentBundle("first"));
        setActivityIntent(intent);
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testSignIn() {

    }

    public void testErrorMessage() {

    }

    public void testNotValidated() {

    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

}
