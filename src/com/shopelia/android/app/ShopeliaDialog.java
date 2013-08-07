package com.shopelia.android.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.shopelia.android.R;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.ActionBar.OnItemClickListener;
import com.shopelia.android.widget.actionbar.ActionBarWidget;
import com.shopelia.android.widget.actionbar.ProgressBarItem;

public class ShopeliaDialog extends Dialog {

    public interface DialogCallback {
        public void onCreateShopeliaActionBar(ShopeliaDialog dialog, ActionBar actionBar);

        public void onActionItemSelected(ShopeliaDialog dialog, Item item);
    }

    private ViewGroup mContainer;

    private View mView;
    private ActionBar mActionBar;

    private DialogCallback mCallback;

    public ShopeliaDialog(Context context) {
        this(context, R.style.Theme_Shopelia_Dialog);
    }

    public ShopeliaDialog(Context context, int theme) {
        super(context, theme);
    }

    public void setDialogCallback(DialogCallback callback) {
        mCallback = callback;
    }

    public View getDecorView() {
        return mContainer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.shopelia_dialog);
        mContainer = (ViewGroup) super.findViewById(R.id.host_container);
        if (mView != null) {
            setContentView(mView);
        }
        mActionBar = new ActionBar(this.getContext());
        mActionBar.bindWidget((ActionBarWidget) super.findViewById(R.id.action_bar));
        mActionBar.setOnItemClickListener(mOnActionBarItemClickListener);
        onCreateShopeliaActionBar(mActionBar);
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(getLayoutInflater().inflate(layoutResID, mContainer, false));
    }

    @Override
    public void setContentView(View view, LayoutParams params) {

    }

    @Override
    public void setContentView(View view) {
        mView = view;
        if (mContainer != null) {
            mContainer.removeAllViews();
            mContainer.addView(view);
        }
    }

    public void startWaiting(CharSequence message, boolean blockUi, boolean isCancelable) {
        getShopeliaActionBar().save();
        getShopeliaActionBar().clear();
        getShopeliaActionBar().addItem(new ProgressBarItem(0, message.toString()));
        getShopeliaActionBar().commit();
    }

    public void stopWaiting() {
        getShopeliaActionBar().restore();
        getShopeliaActionBar().commit();
    }

    @Override
    public void addContentView(View view, LayoutParams params) {
        mContainer.addView(view, params);
    }

    @Override
    public View findViewById(int id) {
        if (mView != null) {
            return mView.findViewById(id);
        }
        return null;
    }

    public ActionBar getShopeliaActionBar() {
        return mActionBar;
    }

    protected void onCreateShopeliaActionBar(ActionBar actionBar) {
        if (mCallback != null) {
            mCallback.onCreateShopeliaActionBar(this, actionBar);
        }
    }

    protected void onActionItemSelected(Item item) {
        if (mCallback != null) {
            mCallback.onActionItemSelected(this, item);
        }
    }

    private OnItemClickListener mOnActionBarItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(Item item) {
            onActionItemSelected(item);
        }
    };

}
