package com.shopelia.android.adapter.form;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.shopelia.android.R;
import com.shopelia.android.adapter.FormAdapter.Field;

public abstract class ButtonField extends Field {

    public static final int TYPE = 2;
    public static int REQUEST_ADDRESS = 0x16;

    private String mHint;
    private String mContentText;
    private String mJsonPath;

    public ButtonField(Context context, int resId) {
        super(TYPE);
        mHint = context.getString(resId);
    }

    @Override
    public long getItemId() {
        return 0;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, ViewGroup viewGroup) {
        View root = inflater.inflate(R.layout.shopelia_form_field_button_field, viewGroup, false);
        ViewHolder holder = new ViewHolder();
        holder.button = (CheckBox) root.findViewById(R.id.address);
        root.setTag(holder);
        return root;
    }

    @Override
    public void bindView(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.button.setOnClickListener(mOnClickListener);
        holder.button.setText(mContentText);
        holder.button.setHint(mHint);
        holder.button.setChecked(isValid());
        holder.button.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    @Override
    public String getJsonPath() {
        return mJsonPath;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }

    public ButtonField setJsonPath(String jsonPath) {
        mJsonPath = jsonPath;
        return this;
    }

    protected void setContentText(String text) {
        mContentText = text;
    }

    private class ViewHolder {
        CheckBox button;
    }

    protected abstract void onClick(Button view);

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            ButtonField.this.onClick((Button) v);
        }
    };

    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked != isValid()) {
                buttonView.setChecked(isValid());
            }
        }
    };

}
