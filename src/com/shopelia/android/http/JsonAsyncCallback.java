package com.shopelia.android.http;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

/**
 * A {@link AsyncCallback} that automatically parse the {@link HttpResponse} as
 * a {@link JSONObject}.
 * 
 * @author Pierre Pollastri
 */
public abstract class JsonAsyncCallback extends AsyncCallback {

    @Override
    public void onComplete(HttpResponse httpResponse) {
        JSONObject object = null;
        try {
            object = new JSONObject(httpResponse.getBodyAsString());
        } catch (JSONException e) {
            Log.e(null, httpResponse.getBodyAsString());
            onError(e);
        }
        if (object != null) {
            onComplete(httpResponse, object);
        }
    }

    public abstract void onComplete(HttpResponse response, JSONObject object);

}
