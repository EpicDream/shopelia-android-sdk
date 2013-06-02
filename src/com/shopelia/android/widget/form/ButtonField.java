package com.shopelia.android.widget.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Checkable;

import com.shopelia.android.R;
import com.shopelia.android.widget.FormEditTextButton;

public abstract class ButtonField extends FormField implements Checkable {

    public static final int TYPE = 2;
    public static int REQUEST_ADDRESS = 0x16;

    private String mHint;
    private String mContentText;
    private String mJsonPath;

    private boolean mChecked = false;

    public ButtonField(Context context) {
        this(context, null);
    }

    public ButtonField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.EditTextField, 0, 0);
            try {
                mContentText = ta.getString(R.styleable.EditTextField_shopelia_text);
                mHint = ta.getString(R.styleable.EditTextField_shopelia_hint);
            } finally {
                ta.recycle();
            }
        }
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
        holder.button.setError(hasError());
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

    private void callClick(Button view) {
    	onClick(view);
    	if (hasOnClickListener()) {
    		getOnClickListener().onClick(this);
    	}
    }
    
    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            callClick((Button) v);
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
    public void setError(boolean hasError) {
        if (hasError) {
            setChecked(false);
        }
        super.setError(hasError);
    }

    @Override
    public void onNextField() {
        super.onNextField();
        mOnClickListener.onClick(null);
    }

}
