package com.shopelia.android;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.app.HostActivity;

public class PincodeActivity extends HostActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.shopelia_process_order_activity);

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_container, new PincodeFragment());
            ft.commit();
        }
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

}
