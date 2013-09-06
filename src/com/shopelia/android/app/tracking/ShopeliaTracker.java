package com.shopelia.android.app.tracking;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

class ShopeliaTracker extends Tracker {

    /**
     * Should run synchronously even for big operations (Delegate are not
     * running on UI thread but in thread pool)
     * 
     * @author Pierre Pollastri
     */
    interface FlushDelegate {
        /**
         * Called on proper thread
         * 
         * @param uuid The uuid of the phone
         * @param trackerName The tracker name
         * @param events The list of events
         * @return The lists of events that the {@link FlushDelegate} was not
         *         able to flush.
         */
        public List<ShopeliaEvent> flush(String uuid, String trackerName, List<ShopeliaEvent> events);
    }

    private static ShopeliaTracker sInstance;
    private WeakReference<Context> mApplicationContext = new WeakReference<Context>(null);
    /**
     * Must be synchronized everywhere
     */
    private HashMap<String, ArrayList<ShopeliaEvent>> mEvents;

    @Override
    public void init(Context context) {
        mApplicationContext = new WeakReference<Context>(context.getApplicationContext());
    }

    @Override
    public void flush() {

    }

    @Override
    public void onDisplayShopeliaButton(String url, String tracker) {
        super.onDisplayShopeliaButton(url, tracker);
    }

    @Override
    public void onClickShopeliaButton(String url, String tracker) {
        super.onClickShopeliaButton(url, tracker);
    }

    public static ShopeliaTracker getInstance() {
        return sInstance == null ? sInstance = new ShopeliaTracker() : sInstance;
    }

    private void save() {

    }

}
