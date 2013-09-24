package com.shopelia.android.remote.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.config.Config;
import com.turbomanage.httpclient.HttpResponse;

import de.greenrobot.event.EventBus;

public abstract class ApiController {

    public static final int ERROR_EXCEPTION = 0;
    public static final int ERROR_HTTP = 1;
    public static final int ERROR_JSON = 2;

    public static class OnApiErrorEvent {
        public final ApiController sender;
        public final HttpResponse response;
        public final Exception exception;
        public final JSONObject json;
        public final int reason;

        private OnApiErrorEvent(ApiController s, HttpResponse r, Exception e, JSONObject j) {
            sender = s;
            response = r;
            exception = e;
            json = j;
            if (response != null) {
                reason = ERROR_HTTP;
            } else if (e != null) {
                reason = ERROR_EXCEPTION;
            } else {
                reason = ERROR_JSON;
            }
        }

    }

    protected static class Event {
        protected Event() {

        }
    }

    protected static class OnResourceEvent<T> extends Event {
        public final T resource;

        protected OnResourceEvent(T resource) {
            this.resource = resource;
        }

    }

    public static class OnAddResourceEvent<T> extends OnResourceEvent<T> {

        protected OnAddResourceEvent(T resource) {
            super(resource);
        }

    }

    public static class OnEditResourceEvent<T> extends OnResourceEvent<T> {

        protected OnEditResourceEvent(T resource) {
            super(resource);
        }

    }

    public static class ErrorInflater {

        public interface Api {
            String ERROR = "error";
            String BASE = "base";
        }

        public static JSONObject inflate(String source) {
            try {
                return new JSONObject(source);
            } catch (JSONException e) {
                JSONObject object = new JSONObject();
                try {
                    object.put(Api.ERROR, source);
                } catch (JSONException e1) {

                }
                return object;
            }
        }

        public static String grabErrorMessage(String source) {
            String message = null;
            try {
                JSONObject error = inflate(source);
                if (error.has(Api.BASE)) {
                    message = error.getJSONArray(Api.BASE).getString(0);
                }
                if (error.has(Api.ERROR)) {
                    message = error.getString(Api.ERROR);
                }
            } catch (Exception e) {

            }
            return message;
        }
    }

    private Context mContext;
    private EventBus mEventBus;

    public ApiController(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public EventBus getEventBus() {
        return mEventBus != null ? mEventBus : (mEventBus = new EventBus());
    }

    public void setEventBus(EventBus eventBus) {
        mEventBus = new EventBus();
    }

    public Class<?>[] getEventTypes() {
        return null;
    }

    public Class<?>[] getStickyEventTypes() {
        return null;
    }

    public void register(Object subscriber) {
        getEventBus().register(subscriber);
    }

    public void unregister(Object subscriber) {
        getEventBus().unregister(subscriber);
    }

    public void registerSticky(Object subscriber) {
        getEventBus().registerSticky(subscriber);
    }

    public Context getContext() {
        return mContext;
    }

    public void stopOrderForError() {

    }

    public void pause() {

    }

    public void resume() {

    }

    protected void fireError(HttpResponse httpResponse, JSONObject response, Exception e) {
        if (Config.INFO_LOGS_ENABLED && e != null) {
            e.printStackTrace();
        }
        getEventBus().post(new OnApiErrorEvent(this, httpResponse, e, response));
    }

}
