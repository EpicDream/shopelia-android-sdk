package com.shopelia.android.http;

import org.json.JSONException;
import org.json.JSONObject;

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
            onError(e);
        }
        if (object != null) {
            onComplete(object);
        }
    }

    public abstract void onComplete(JSONObject object);

}
