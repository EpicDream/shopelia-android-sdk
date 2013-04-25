package com.shopelia.android;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.app.HostActivity;

public class ProcessOrderActivity extends HostActivity {

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);
        setHostContentView(R.layout.shopelia_process_order_activity);

        if (saveState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_container, new ProcessOrderFragment());
            ft.commit();
        }

    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return true;
    }

}
