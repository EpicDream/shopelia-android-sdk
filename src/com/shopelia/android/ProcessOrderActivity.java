package com.shopelia.android;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.shopelia.android.ProcessOrderFragment.OrderHandlerHolder;
import com.shopelia.android.app.HostActivity;
import com.shopelia.android.remote.api.OrderHandler;

public class ProcessOrderActivity extends HostActivity implements OrderHandlerHolder {

    private OrderHandler mOrderHandler;

    ProcessOrderFragment mProcessOrderFragment = new ProcessOrderFragment();

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_process_order_activity);

        if (saveState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, mProcessOrderFragment);
            ft.commit();
        }

        if (mOrderHandler == null) {
            mOrderHandler = new OrderHandler(this, mProcessOrderFragment);
        }

    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return true;
    }

    @Override
    public OrderHandler getOrderHandler() {
        return mOrderHandler;
    }

    @Override
    public void askForConfirmation() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new ConfirmationFragment());
        ft.commit();
    }

    @Override
    public void confirm() {
        getOrderHandler().confirm();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getOrderHandler().cancel();
    }

    @Override
    public void onCheckoutSucceed() {
        // TODO Display success screen
        Toast.makeText(this, "Order succeed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCheckoutFailed() {
        // TODO Display failure screen
        Toast.makeText(this, "Order failed", Toast.LENGTH_LONG).show();
    }

}
