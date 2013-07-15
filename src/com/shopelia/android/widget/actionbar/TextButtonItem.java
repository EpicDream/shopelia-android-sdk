package com.shopelia.android.widget.actionbar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.R;
import com.shopelia.android.widget.actionbar.ActionBar.Item;

public class TextButtonItem extends Item {

    private String mText;
    private int mDividerVisibility = View.VISIBLE;

    public TextButtonItem(int id) {
        super(id);
    }

    public TextButtonItem(int id, String text) {
        super(id);
        mText = text;
    }

    public TextButtonItem(int id, Context context, int textId) {
        this(id, context.getString(textId));
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.shopelia_action_bar_text_button_item, container, false);
    }

    @Override
    public void bindView(View view) {
        TextView textView = (TextView) view.findViewById(R.id.action_bar_item_textview);
        textView.setText(mText);
        view.findViewById(R.id.divider).setVisibility(mDividerVisibility);
    }

    public void setDividerVisibility(int dividerVisibility) {
        this.mDividerVisibility = dividerVisibility;
    }

    public static TextButtonItem createTextViewItem(int id, CharSequence text, boolean hasDivider) {
        TextButtonItem item = new TextButtonItem(id, text.toString());
        item.setDividerVisibility(hasDivider ? View.VISIBLE : View.GONE);
        item.setClickable(false);
        return item;
    }
}
