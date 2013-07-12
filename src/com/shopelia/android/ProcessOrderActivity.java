package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.AuthenticateFragment.OnUserAuthenticateListener;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.manager.UserManager;

public class ProcessOrderActivity extends ShopeliaActivity implements OnUserAuthenticateListener {

    public static final String ACTIVITY_NAME = "Confirmation";

    private ConfirmationFragment mConfirmationFragment = new ConfirmationFragment();

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_process_order_activity);

        if (saveState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, mConfirmationFragment);
            ft.commit();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECKOUT) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return true;
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

    @Override
    public void onUserAuthenticate(boolean authoSignIn) {
        mConfirmationFragment.setupUi(UserManager.get(this).getUser());
    }

}
