package com.shopelia.android;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.app.HostActivity;

public class StartActivity extends HostActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getIntent().putExtra(EXTRA_INIT_ORDER, true);
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.shopelia_start_activity);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new SignUpFragment());
        ft.commit();

    }
}
