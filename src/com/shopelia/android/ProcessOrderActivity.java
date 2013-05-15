package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.shopelia.android.ProcessOrderFragment.OrderHandlerHolder;
import com.shopelia.android.app.HostActivity;
import com.shopelia.android.remote.api.OrderHandler;

public class ProcessOrderActivity extends HostActivity implements OrderHandlerHolder {

    public static final String ACTIVITY_NAME = "ProcessOrder";

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
    protected void onResume() {
        super.onResume();
        if (mOrderHandler != null) {
            mOrderHandler.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOrderHandler != null) {
            mOrderHandler.pause();
        }
    }

    @Override
    public void confirm() {
        getOrderHandler().confirm();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getOrderHandler().cancel();
        getOrderHandler().recycle();
    }

    @Override
    public void onCheckoutSucceed() {
        // TODO Display success screen
        startActivity(new Intent(this, CloseCheckoutActivity.class));
        getOrderHandler().recycle();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onCheckoutFailed() {
        // TODO Display failure screen
        setResult(RESULT_FAILURE);
        Toast.makeText(this, "Order failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

    @Override
    public void finalizeOrder() {
        mProcessOrderFragment = ProcessOrderFragment.createFinalization();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, mProcessOrderFragment);
        ft.commit();
    }

}
