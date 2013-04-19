package com.shopelia.android.adapter.form;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.shopelia.android.R;
import com.shopelia.android.adapter.FormAdapter.Field;
import com.shopelia.utils.CharSequenceUtils;

public class EditTextField extends Field {

    public static final int TYPE = 1;

    private String mContentText;
    private String mHint;

    public EditTextField(String defaultText, String hint) {
        super(TYPE);
        mContentText = defaultText;
        mHint = hint;
    }

    public EditTextField(Context context, int hintResId) {
        this(null, context.getString(hintResId));
    }

    @Override
    public long getItemId() {
        return 0;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.shopelia_form_field_edit_text, viewGroup, false);
        ViewHolder holder = new ViewHolder();
        holder.editText = (EditText) view.findViewById(R.id.edit_text);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        setViewStyle(holder);
        holder.editText.setHint(mHint);
        if (!CharSequenceUtils.isEmpty(mContentText)) {
            holder.editText.setText(mContentText);
        }
    }

    protected void setViewStyle(ViewHolder holder) {
        // Here for extension purpose so we can later create PasswordField,
        // EmailField... just by extending EditTextField and overriding this
        // method.
    }

    @Override
    public Object getResult() {
        return mContentText;
    }

    @Override
    public String getJsonPath() {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

    }

    public static class ViewHolder {
        EditText editText;
    }

}
