package com.shopelia.android;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.SignUpFragment.OnSignUpListener;
import com.shopelia.android.app.HostActivity;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.Order;

public class StartActivity extends HostActivity implements OnSignUpListener {

    public static final String EXTRA_PRODUCT_URL = Config.EXTRA_PREFIX + "PRODUCT_URL";

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

    @Override
    public void onSignUp(JSONObject result) {
        Order order = Order.inflate(result);
        order.productUrl = getIntent().getStringExtra(EXTRA_PRODUCT_URL);
        Intent intent = new Intent(this, ProcessOrderActivity.class);
        intent.putExtra(HostActivity.EXTRA_ORDER, order);
        startActivityForResult(intent, Config.REQUEST_ORDER);
    }
}
