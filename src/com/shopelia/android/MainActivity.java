package com.shopelia.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.shopelia.android.api.Command;
import com.shopelia.android.api.ShopeliaRestClient;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParameterMap params = ShopeliaRestClient.newParams();
        params.add("Test", "42").add("Restest", "test");
        JSONObject root = new JSONObject();
        try {
            root.put("test", 42);
            root.put("Retest", "yo");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ShopeliaRestClient.post(Command.V1.Users.$, root, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
