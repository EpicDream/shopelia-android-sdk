package com.shopelia.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.shopelia.android.WelcomeFragment.WelcomeParent;
import com.shopelia.android.api.Shopelia;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Merchant;

public class WelcomeActivity extends ShopeliaActivity implements WelcomeParent {

    public static final String ACTIVITY_NAME = "Welcome Activity";

    public static final int REQUEST_SHOPELIA = 0x100;
    public static final int REQUEST_MERCHANT = 0x101;

    public boolean mIsCanceling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UserManager.get(this).getLoginsCount() > 0) {
            setHostContentView(R.layout.shopelia_welcome_activity);
            getShopeliaActionBar().hide();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, new WelcomeFragment());
            ft.commit();
        } else if (UserManager.get(this).getCheckoutCount() > 0) {
            startActivityForResult(new Intent(this, PrepareOrderActivity.class), 0);
        } else {
            setHostContentView(R.layout.shopelia_welcome_activity);
            getShopeliaActionBar().hide();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, new WelcomeFragment());
            ft.commit();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtras(getIntent().getExtras());
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MERCHANT) {
            resultCode = Shopelia.RESULT_REDIRECT_ON_MERCHANT;
        }
        setResult(resultCode, data);
        finish();
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

    @Override
    public void continueWithShopelia() {
        startActivityForResult(new Intent(this, PrepareOrderActivity.class), 0);
    }

    @Override
    public void continueWithMerchant() {
        String url = getIntent().getExtras().getString(Shopelia.EXTRA_PRODUCT_URL);
        super.startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(url)), REQUEST_MERCHANT);
    }

    @Override
    public Merchant getMerchant() {
        return getIntent().getExtras().getParcelable(Shopelia.EXTRA_MERCHANT);
    }

    @Override
    public void cancel() {
        if (mIsCanceling) {
            return;
        }
        setResult(Shopelia.RESULT_CANCELED);
        mIsCanceling = true;
        View frame = findViewById(R.id.frame);
        if (frame != null) {
            try {
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.shopelia_pop_out);
                anim.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        finish();
                    }
                });
                frame.startAnimation(anim);
            } catch (Exception e) {
                // XML Pull Parser failed to load the Animation
                finish();
            }
        } else {
            // finish();
        }
    }

    @Override
    public void onBackPressed() {
        cancel();
    }

}
