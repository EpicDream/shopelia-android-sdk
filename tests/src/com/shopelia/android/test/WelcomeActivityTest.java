package com.shopelia.android.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;
import com.shopelia.android.WelcomeActivity;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.test.fixtures.FixtureBundleFactory;

@SuppressLint("NewApi")
public class WelcomeActivityTest extends ActivityInstrumentationTestCase2<WelcomeActivity> {

    private Solo solo;

    public WelcomeActivityTest() {
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
    }

    public void testShouldBeTranslucent() {
        assertEquals(ShopeliaActivity.STYLE_TRANSLUCENT, getActivity().getActivityStyle());
    }

    public void testClickOnGoToMerchantSite() {
        solo.clickOnView(solo.getView(R.id.continue_with_merchant_site));
        solo.goBack();
        solo.assertCurrentActivity("Should be on WelcomeActivity", WelcomeActivity.class);
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

}
