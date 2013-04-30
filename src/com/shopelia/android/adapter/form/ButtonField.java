package com.shopelia.android.adapter.form;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Checkable;

import com.shopelia.android.R;
import com.shopelia.android.adapter.FormAdapter.Field;
import com.shopelia.android.widget.Errorable;
import com.shopelia.android.widget.FormEditTextButton;

public abstract class ButtonField extends Field implements Errorable, Checkable {

    public static final int TYPE = 2;
    public static int REQUEST_ADDRESS = 0x16;

    private String mHint;
    private String mContentText;
    private String mJsonPath;

    private boolean mChecked = false;
    private boolean mError = false;

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
        holder.button = (FormEditTextButton) root.findViewById(R.id.address);
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
        holder.button.setError(mError);
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
        FormEditTextButton button;
    }

    protected abstract void onClick(Button view);

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            ButtonField.this.onClick((Button) v);
        }
    };

    public boolean isChecked() {
        return mChecked;
    };

    @Override
    public void setChecked(boolean checked) {
        if (isChecked() != checked) {
            mChecked = checked;
            if (checked) {
                setError(false);
            }

            if (getBoundedView() != null) {
                bindView(getBoundedView());
            }
        }
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public boolean hasError() {
        return mError;
    }

    @Override
    public void setError(boolean hasError) {
        if (hasError() != hasError) {
            mError = hasError;
            if (hasError) {
                setChecked(false);
            }

            if (getBoundedView() != null) {
                bindView(getBoundedView());
            }
        }
    }

}
