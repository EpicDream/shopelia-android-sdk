package com.shopelia.android;

import android.os.Bundle;

import com.shopelia.android.app.HostActivity;
import com.shopelia.android.manager.UserManager;

public class StartActivity extends HostActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.activity_main);

        if (UserManager.get(this).isLogged()) {

        } else {

        }

    }

}
