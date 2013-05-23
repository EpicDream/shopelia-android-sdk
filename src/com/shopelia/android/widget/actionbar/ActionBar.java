package com.shopelia.android.widget.actionbar;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

/**
 * A Controller class managing the Shopelia's action bar. This class holds the
 * logical structure of the action bar and bound it to an
 * {@link ActionBarWidget}
 * 
 * @author Pierre Pollastri
 */
public class ActionBar {

    public abstract static class Item {

    }

    private Context mContext;
    private ActionBarWidget mActionBarWidget;
    private List<Item> mItems = new ArrayList<Item>();
    private List<Item> mNotCommittedItems = new ArrayList<ActionBar.Item>();

    public ActionBar(Context context, ActionBarWidget boundedWidget) {
        mContext = context;
        mActionBarWidget = boundedWidget;
    }

    public ActionBar addItem(Item item) {
        return this;
    }

    public void commit() {

    }

    public Context getContext() {
        return mContext;
    }

    public ActionBarWidget getView() {
        return mActionBarWidget;
    }

}
