package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.AuthenticateFragment.OnUserAuthenticateListener;
import com.shopelia.android.SingleAddressFragment.OnAddressChangeListener;
import com.shopelia.android.SinglePaymentCardFragment.OnPaymentCardChangeListener;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.remote.api.AddressesAPI;
import com.shopelia.android.remote.api.ApiController.CallbackAdapter;

public class ProcessOrderActivity extends ShopeliaActivity implements OnUserAuthenticateListener, OnAddressChangeListener,
        OnPaymentCardChangeListener {

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

    @Override
    public void onAddressChange(Address address) {
        getOrder().address = address;
        new AddressesAPI(this, new CallbackAdapter()).setDefaultAddress(address);
    }

    @Override
    public void onPaymentCardChange(PaymentCard card) {
        getOrder().card = card;
    }

}
