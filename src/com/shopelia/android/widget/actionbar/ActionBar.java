package com.shopelia.android.widget.actionbar;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.shopelia.android.utils.StateList;

/**
 * A Controller class managing the Shopelia's action bar. This class holds the
 * logical structure of the action bar and bound it to an
 * {@link ActionBarWidget}
 * 
 * @author Pierre Pollastri
 */
public class ActionBar {

    public interface OnItemClickListener {
        public void onItemClick(Item item);
    }

    public abstract static class Item {

        private final int mId;
        private View mView;

        public Item(int id) {
            mId = id;
        }

        public int getId() {
            return mId;
        }

        public abstract View getView(LayoutInflater inflater, ViewGroup container);

        public abstract void bindView(View view);

        public View getView() {
            return mView;
        }

        protected View getView(ActionBar actionBar) {
            if (mView == null) {
                mView = getView(actionBar.getLayoutInflater(), actionBar.getView().getOptionsContainer());
            }
            bindView(mView);
            return mView;
        }

        protected void invalidate() {
            if (mView != null) {
                bindView(mView);
            }
        }

    }

    private Context mContext;
    private ActionBarWidget mActionBarWidget;
    private StateList<Item> mItems = new StateList<ActionBar.Item>();
    private LinkedList<StateList<Item>> mSavedStates = new LinkedList<StateList<Item>>();
    private LayoutInflater mInflater;
    private OnItemClickListener mOnItemClickListener;

    public ActionBar(Context context) {
        mContext = context;
    }

    public void bindWidget(ActionBarWidget boundWidget) {
        mInflater = LayoutInflater.from(mContext);
        mActionBarWidget = boundWidget;
        invalidate();
    }

    public ActionBar addItem(Item item) {
        mItems.add(item);
        return this;
    }

    public void commit() {
        if (mActionBarWidget == null) {
            mItems.commit();
            return;
        }
        if (mItems.size() > 0) {
            mActionBarWidget.swapBuffer();
            doCommit();
        } else {
            doCommit();
        }
    }

    public void clear() {
        mItems.clear();
    }

    public Context getContext() {
        return mContext;
    }

    public ActionBarWidget getView() {
        return mActionBarWidget;
    }

    public LayoutInflater getLayoutInflater() {
        return mInflater;
    }

    public void invalidate() {
        ViewGroup container = mActionBarWidget.getOptionsContainer();
        container.removeAllViews();
        for (Item item : mItems) {
            View view = item.getView(this);
            if (view.getParent() != null && view.getParent() instanceof ViewGroup) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
            view.setOnClickListener(mOnClickListener);
            container.addView(view);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void save() {
        mSavedStates.add(new StateList<ActionBar.Item>(mItems));
    }

    public void restore() {
        try {
            mItems = mSavedStates.getLast();
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("Too much call to restore", e);
        }
    }

    private void doCommit() {
        mItems.commit();
        invalidate();
    }

    public void hide() {
        mActionBarWidget.setVisibility(View.GONE);
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener == null) {
                return;
            }
            for (Item item : mItems) {
                if (v == item.getView()) {
                    mOnItemClickListener.onItemClick(item);
                }
            }
        }
    };

}
