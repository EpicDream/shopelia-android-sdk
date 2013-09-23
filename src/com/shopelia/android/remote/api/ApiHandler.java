package com.shopelia.android.remote.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.config.Config;
import com.turbomanage.httpclient.HttpResponse;

import de.greenrobot.event.EventBus;

public abstract class ApiHandler {

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

    private static Class<?> sEventTypeCache;
    private static Class<?>[] sMoreEventTypesCache;
    private static Class<?> sStickyEventTypeCache;
    private static Class<?>[] sMoreStickyEventTypesCache;

    public ApiHandler(Context context) {
        this.mContext = context;
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
        if (GetEventType(this) != null) {
            getEventBus().register(subscriber, GetEventType(this), GetMoreEventTypes(this));
        }
        if (GetStickyEventType(this) != null) {
            getEventBus().registerSticky(subscriber, GetStickyEventType(this), GetMoreStickyEventTypes(this));
        }
    }

    public void unregister(Object subscriber) {
        if (GetEventType(this) != null) {
            getEventBus().unregister(subscriber, getEventTypes());
        }
        if (GetStickyEventType(this) != null) {
            getEventBus().unregister(subscriber, getStickyEventTypes());
        }
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

    protected void fireError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
        if (Config.INFO_LOGS_ENABLED && e != null) {
            e.printStackTrace();
        }
    }

    private static final Class<?> GetEventType(ApiHandler handler) {
        GetMoreEventTypes(handler);
        return sEventTypeCache;
    }

    private static final Class<?>[] GetMoreEventTypes(ApiHandler handler) {
        if (sEventTypeCache == null || sMoreEventTypesCache == null) {
            Class<?>[] classes = handler.getEventTypes();
            if (classes != null && classes.length > 0) {
                sEventTypeCache = classes[0];
                sMoreEventTypesCache = new Class<?>[classes.length - 1];
                for (int index = 1; index < classes.length; index++) {
                    sMoreEventTypesCache[index - 1] = classes[index];
                }
            }
        }
        return sMoreEventTypesCache;
    }

    private static final Class<?> GetStickyEventType(ApiHandler handler) {
        GetMoreStickyEventTypes(handler);
        return sStickyEventTypeCache;
    }

    private static final Class<?>[] GetMoreStickyEventTypes(ApiHandler handler) {
        if (sStickyEventTypeCache == null || sMoreStickyEventTypesCache == null) {
            Class<?>[] classes = handler.getStickyEventTypes();
            if (classes != null && classes.length > 0) {
                sStickyEventTypeCache = classes[0];
                sMoreStickyEventTypesCache = new Class<?>[classes.length - 1];
                for (int index = 1; index < classes.length; index++) {
                    sMoreStickyEventTypesCache[index - 1] = classes[index];
                }
            }
        }
        return sMoreStickyEventTypesCache;
    }

}
