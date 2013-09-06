package com.shopelia.android.app.tracking;

import java.util.LinkedList;

import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.model.User;

class DummyTracker extends Tracker {

    public static final LinkedList<Entry> LOGS = new LinkedList<DummyTracker.Entry>();

    @Override
    public void init(Context context) {

    }

    @Override
    public void track(String eventName) {
        LOGS.add(new Entry(eventName));
    }

    @Override
    public void track(String eventName, JSONObject object) {
        LOGS.add(new Entry(eventName, object.toString()));
    }

    @Override
    public void onDisplay(String activityName) {
        LOGS.add(new Entry(Analytics.Events.Activities.DISPLAY, activityName));
    }

    @Override
    public void onFocusIn(String fieldName) {
        LOGS.add(new Entry(Analytics.Events.UserInteractions.FOCUS_IN, fieldName));
    }

    @Override
    public void onValidate(String fieldName) {
        LOGS.add(new Entry(Analytics.Events.UserInteractions.OK, fieldName));
    }

    @Override
    public void flush() {

    }

    @Override
    public void identify(User user) {
        LOGS.add(new Entry(Analytics.Events.IDENTIFY, user.email));
    }

    @Override
    public void unidentify() {
        LOGS.add(new Entry(Analytics.Events.UNIDENTIFY));
    }

    private static class Entry {
        String eventName;
        String extras;

        public Entry(String eventName) {
            this.eventName = eventName;
        }

        public Entry(String eventName, String extras) {
            this.eventName = eventName;
            this.extras = extras;
        }

    }

}
