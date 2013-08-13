package com.shopelia.android.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;
import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.ProcessOrderActivity;
import com.shopelia.android.SignInFragment;
import com.shopelia.android.WelcomeActivity;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.User;
import com.shopelia.android.test.fixtures.FixtureBundleFactory;
import com.shopelia.android.test.model.UserFactory;
import com.shopelia.android.widget.form.PasswordField;

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
        user = UserFactory.get("me");
        TestUtils.signIn(getInstrumentation().getTargetContext(), user);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(FixtureBundleFactory.getShopeliaIntentBundle("first"));
        setActivityIntent(intent);
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testLogout() {
        solo.clickOnText(getActivity().getString(R.string.shopelia_action_bar_sign_out));
        solo.waitForText(getActivity().getString(R.string.shopelia_logout_are_you_sure), 1, 100000);
        solo.clickOnText(getActivity().getString(R.string.shopelia_logout_positive));
        solo.waitForDialogToClose(1000);
        int death = 2000;
        while (solo.getCurrentActivity() instanceof WelcomeActivity) {
            solo.sleep(50);
            if (death-- < 0) {
                fail("Still on Authenticate activity");
            }
        }
        assertFalse("Should be signed out", UserManager.get(getActivity()).isLogged());
        solo.assertCurrentActivity("Should be on PrepareOrder", PrepareOrderActivity.class);
        PrepareOrderActivity activity = (PrepareOrderActivity) solo.getCurrentActivity();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        assertTrue("Should be on Sign in Fragment", fragment instanceof SignInFragment);
    }

    public void testAuthenticate() {
        PasswordField field = (PasswordField) solo.getView(R.id.password);
        solo.typeText(field.getEditText(), user.password);
        solo.clickOnText(getActivity().getString(R.string.shopelia_authenticate_validate));
        solo.waitForActivity(ProcessOrderActivity.class, 10000);
        solo.assertCurrentActivity("Should be on process order", ProcessOrderActivity.class);
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

}
