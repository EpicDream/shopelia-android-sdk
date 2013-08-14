package com.shopelia.android.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;
import com.shopelia.android.PrepareOrderActivity;
import com.shopelia.android.WelcomeActivity;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.model.User;
import com.shopelia.android.test.fixtures.FixtureBundleFactory;
import com.shopelia.android.test.model.UserFactory;

@SuppressLint("NewApi")
public class WelcomeFragmentTest extends ActivityInstrumentationTestCase2<WelcomeActivity> {

    private Solo solo;

    public WelcomeFragmentTest() {
        super(WelcomeActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        User user = UserFactory.get("me");
        TestUtils.signIn(getInstrumentation().getTargetContext(), user);
        TestUtils.signOut(null);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(FixtureBundleFactory.getShopeliaIntentBundle("first"));
        setActivityIntent(intent);
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testShouldBeTranslucent() {
        assertEquals(ShopeliaActivity.STYLE_TRANSLUCENT, getActivity().getActivityStyle());
    }

    public void testClickOnGoToMerchantSite() {
        solo.clickOnView(solo.getView(R.id.continue_with_merchant_site));
        solo.goBack();
        solo.assertCurrentActivity("Should be on WelcomeActivity", WelcomeActivity.class);
    }

    public void testClickOnContinueToShopelia() {
        solo.clickOnView(solo.getView(R.id.continue_with_shopelia));
        solo.assertCurrentActivity("Should be on PrepareOrderActivity", PrepareOrderActivity.class);
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

}
