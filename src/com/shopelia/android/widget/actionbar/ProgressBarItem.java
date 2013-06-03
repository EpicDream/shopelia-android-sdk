package com.shopelia.android.widget.actionbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.R;
import com.shopelia.android.widget.actionbar.ActionBar.Item;

public class ProgressBarItem extends Item {

    private String mText;

    public ProgressBarItem(int id) {
        this(id, null);
    }

    public ProgressBarItem(int id, String text) {
        super(id);
        mText = text;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.shopelia_action_bar_progress_bar_item, container, false);
    }

    @Override
    public void bindView(View view) {
        TextView textView = (TextView) view.findViewById(R.id.action_bar_item_textview);
        textView.setText(mText);
        view.setEnabled(false);
    }
}
