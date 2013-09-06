package com.shopelia.android.app.tracking;

import org.json.JSONException;
import org.json.JSONObject;

import com.shopelia.android.model.JsonData;
import com.shopelia.android.utils.DigestUtils;

class ShopeliaEvent implements JsonData {

    interface Api {
        String DIGEST = "digest";
        String SENT_AT = "sent_at";
    }

    public String digest;
    public String action;
    public String url;
    public long sent_at;

    public ShopeliaEvent() {

    }

    public ShopeliaEvent(String action, String url) {
        this.action = action;
        this.url = url;
        digest = DigestUtils.md5(action + "://" + url);
    }

    public void notifySent() {
        sent_at = System.currentTimeMillis();
    }

    @Override
    public int hashCode() {
        return digest != null ? digest.hashCode() : 0;
    }

    public static ShopeliaEvent inflate(JSONObject object) throws JSONException {
        ShopeliaEvent event = new ShopeliaEvent();
        event.digest = object.getString(Api.DIGEST);
        event.sent_at = object.getLong(Api.SENT_AT);
        return event;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(Api.DIGEST, digest);
        object.put(Api.SENT_AT, sent_at);
        return object;
    }

}
