package com.shopelia.android;

import android.os.Bundle;

import com.shopelia.android.app.BaseActivity;
import com.shopelia.android.manager.UserManager;

public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (UserManager.get(this).isLogged()) {

        } else {

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
