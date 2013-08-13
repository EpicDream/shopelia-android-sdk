package com.shopelia.android.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Condition;
import com.jayway.android.robotium.solo.Solo;
import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.ProcessOrderActivity;
import com.shopelia.android.RecoverPasswordActivity;
import com.shopelia.android.SignInFragment;
import com.shopelia.android.model.User;
import com.shopelia.android.test.fixtures.FixtureBundleFactory;
import com.shopelia.android.test.model.UserFactory;
import com.shopelia.android.widget.form.EditTextField;
import com.shopelia.android.widget.form.EmailField;

public class SignInFragmentTest extends ActivityInstrumentationTestCase2<PrepareOrderActivity> {

    Solo solo;
    User user;

    @SuppressLint("NewApi")
    public SignInFragmentTest() {
        super(PrepareOrderActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = UserFactory.get("me");
        TestUtils.signOut(getInstrumentation().getTargetContext());
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(FixtureBundleFactory.getShopeliaIntentBundle("first"));
        setActivityIntent(intent);
        solo = new Solo(getInstrumentation(), getActivity());
        if (!(getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof SignInFragment)) {
            solo.clickOnText(getActivity().getString(R.string.shopelia_action_bar_sign_in));
            solo.waitForText(getActivity().getString(R.string.shopelia_action_bar_sign_up));
        }
    }

    public void testSignIn() {
        solo.typeText(solo.getView(EditTextField.class, 0).getEditText(), user.email);
        solo.typeText(solo.getView(EditTextField.class, 1).getEditText(), user.password);
        solo.clickOnView(solo.getView(R.id.validate));
        solo.waitForActivity(ProcessOrderActivity.class, 20000);
        solo.assertCurrentActivity("Should be on process activity", ProcessOrderActivity.class);
    }

    public void testErrorMessage() {
        solo.typeText(solo.getView(EditTextField.class, 0).getEditText(), user.email);
        solo.typeText(solo.getView(EditTextField.class, 1).getEditText(), "wrong password" + user.password);
        solo.clickOnView(solo.getView(R.id.validate), true);
        solo.waitForCondition(new Condition() {

            @Override
            public boolean isSatisfied() {
                return !getActivity().isInWaitingMode();
            }
        }, 20000);
    }

    public void testNotValidated() {
        solo.typeText(solo.getView(EditTextField.class, 0).getEditText(), "A wrong email all dirty");
        solo.typeText(solo.getView(EditTextField.class, 1).getEditText(), "n");
        solo.clickOnView(solo.getView(R.id.validate), true);
        solo.sleep(200);
        assertFalse(solo.getView(EditTextField.class, 0).isValid());
    }

    public void testForgotPassword() {
        solo.clickOnView(solo.getView(R.id.forgotPassword));
        solo.waitForActivity(RecoverPasswordActivity.class, 10000);
    }

    public void testForgotPasswordWithPrefilledEmail() {
        solo.typeText(solo.getView(EditTextField.class, 0).getEditText(), user.email);
        solo.clickOnView(solo.getView(R.id.forgotPassword));
        solo.waitForActivity(RecoverPasswordActivity.class, 10000);
        assertEquals(solo.getView(EmailField.class, 0).getResultAsString(), user.email);
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

}
