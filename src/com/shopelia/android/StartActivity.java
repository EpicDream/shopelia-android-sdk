package com.shopelia.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.app.HostActivity;

public class StartActivity extends HostActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getIntent().putExtra(EXTRA_INIT_ORDER, true);
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.shopelia_start_activity);

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, new SignUpFragment());
            ft.commit();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
