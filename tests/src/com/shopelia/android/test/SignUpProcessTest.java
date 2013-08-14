package com.shopelia.android.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Condition;
import com.shopelia.android.AddAddressActivity;
import com.shopelia.android.AddPaymentCardActivity;
import com.shopelia.android.CloseCheckoutActivity;
import com.shopelia.android.CreatePasswordFragment;
import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.ProcessOrderActivity;
import com.shopelia.android.SignUpFragment;
import com.shopelia.android.model.User;
import com.shopelia.android.test.fixtures.FixtureBundleFactory;
import com.shopelia.android.test.model.UserFactory;
import com.shopelia.android.test.robotium.Solo2;

public class SignUpProcessTest extends ActivityInstrumentationTestCase2<PrepareOrderActivity> {

    Solo2 solo;
    User user;

    @SuppressLint("NewApi")
    public SignUpProcessTest() {
        super(PrepareOrderActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.signOut(getInstrumentation().getTargetContext());
        user = UserFactory.get("test");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(FixtureBundleFactory.getShopeliaIntentBundle("first"));
        setActivityIntent(intent);
        solo = new Solo2(getInstrumentation(), getActivity());
        if (!(getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof SignUpFragment)) {
            solo.clickOnText(getActivity().getString(R.string.shopelia_action_bar_sign_up));
            solo.waitForText(getActivity().getString(R.string.shopelia_action_bar_sign_in));
        }
    }

    public void testSignUp() {
        solo.enterTextAndNext(solo.getEditText(getActivity().getString(R.string.shopelia_form_main_email)), user.email);
        solo.enterTextAndNext((EditText) getActivity().getCurrentFocus(), user.getDefaultAddress().phone);
        solo.waitForActivity(AddAddressActivity.class);
        solo.enterTextAndNext(solo.getEditText(getActivity().getString(R.string.shopelia_form_address_first_name)), user.firstname);
        solo.enterTextAndNext((EditText) solo.getCurrentActivity().getCurrentFocus(), user.lastname);
        solo.enterTextAndNext((EditText) solo.getCurrentActivity().getCurrentFocus(), user.getDefaultAddress().address);
        solo.enterTextAndNext((EditText) solo.getCurrentActivity().getCurrentFocus(), user.getDefaultAddress().extras);
        solo.enterTextAndNext((EditText) solo.getCurrentActivity().getCurrentFocus(), user.getDefaultAddress().zipcode);
        solo.enterTextAndNext((EditText) solo.getCurrentActivity().getCurrentFocus(), user.getDefaultAddress().city);
        solo.clearEditText((EditText) solo.getCurrentFocus());
        solo.enterTextAndNext((EditText) solo.getCurrentActivity().getCurrentFocus(), user.getDefaultAddress().getDisplayCountry());
        solo.clickOnView(solo.getView(R.id.validate), true);
        solo.waitForActivity(PrepareOrderActivity.class);
        solo.clickOnView(solo.getView(R.id.validate), true);
        solo.waitForActivity(AddPaymentCardActivity.class, 10000);
        solo.enterTextAndNext(solo.getEditText(getActivity().getString(R.string.shopelia_form_payment_card_number)),
                user.getDefaultPaymentCard().number);
        solo.enterText((EditText) solo.getCurrentFocus(), user.getDefaultPaymentCard().expMonth);
        solo.enterTextAndNext((EditText) solo.getCurrentFocus(), user.getDefaultPaymentCard().expYear);
        solo.enterTextAndNext((EditText) solo.getCurrentFocus(), user.getDefaultPaymentCard().cvv);
        solo.clickOnView(solo.getView(R.id.validate), true);
        solo.waitForActivity(ProcessOrderActivity.class);
        solo.waitForFragmentById(R.id.fragment_container);
        solo.waitForCondition(new Condition() {

            @Override
            public boolean isSatisfied() {
                return solo.getView(R.id.validate).isEnabled();
            }
        }, 2000);
        solo.clickOnView(solo.getView(R.id.validate));
        solo.waitForActivity(CloseCheckoutActivity.class);
        solo.waitForFragmentById(R.id.fragment_container);
        assertTrue("Should propose for password creation",
                solo.searchText(solo.getString(R.string.shopelia_close_checkout_create_a_password).toUpperCase(), true));
        solo.clickOnText(solo.getString(R.string.shopelia_close_checkout_create_a_password).toUpperCase());
        solo.waitForFragmentByTag(CreatePasswordFragment.TAG);
        solo.enterTextAndNext(solo.getEditText(solo.getString(R.string.shopelia_form_password_password)), user.password);
        solo.enterTextAndNext((EditText) solo.getCurrentFocus(), user.password);
        solo.clickOnText(solo.getString(R.string.shopelia_form_password_validate).toUpperCase());
        solo.waitForText(solo.getString(R.string.shopelia_form_password_waiting), 1, 1000);
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

}
