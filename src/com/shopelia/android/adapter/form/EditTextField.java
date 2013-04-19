package com.shopelia.android.adapter.form;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.R;
import com.shopelia.android.adapter.FormAdapter;
import com.shopelia.android.adapter.FormAdapter.Field;
import com.shopelia.android.widget.FormEditText;
import com.shopelia.utils.CharSequenceUtils;

public class EditTextField extends Field {

    public abstract static class OnValidateListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public abstract boolean onValidate(EditTextField editTextField, boolean shouldFireError);

    }

    public static final int TYPE = 1;

    private String mContentText;
    private String mHint;
    private OnValidateListener mOnValidateListener;
    private boolean mAllowEmptyContent = false;
    private boolean mAutoTrim = true;
    private String mJsonPath;

    public EditTextField(String defaultText, String hint) {
        super(TYPE);
        mContentText = defaultText;
        mHint = hint;
    }

    public EditTextField(Context context, String defaultText, int hintResId) {
        this(null, context.getString(hintResId));
    }

    public EditTextField setOnValidateListener(OnValidateListener listener) {
        mOnValidateListener = listener;
        setContentText(mContentText);
        return this;
    }

    public EditTextField setAllowingEmptyContent(boolean allowEmptyContent) {
        mAllowEmptyContent = allowEmptyContent;
        return this;
    }

    public EditTextField setAutoTrimEnable(boolean autoTrim) {
        mAutoTrim = autoTrim;
        return this;
    }

    public EditTextField setJsonPath(String... jsonPath) {
        StringBuilder builder = new StringBuilder();
        if (jsonPath != null) {
            for (String item : jsonPath) {
                builder.append(item);
                builder.append(FormAdapter.PATH_SEPARATOR);
            }
            if (builder.length() > 0) {
                builder.deleteCharAt(builder.length() - 1);
            }
        }
        mJsonPath = builder.toString();
        return this;
    }

    @Override
    public long getItemId() {
        return 0;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.shopelia_form_field_edit_text, viewGroup, false);
        ViewHolder holder = new ViewHolder();
        holder.editText = (FormEditText) view.findViewById(R.id.edit_text);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.editText.removeTextChangedListener(holder.textWatcher);
        holder.textWatcher = mTextWatcher;
        if (mTextWatcher != null) {
            holder.editText.addTextChangedListener(mTextWatcher);
        }
        holder.editText.setOnFocusChangeListener(mOnFocusChangeListener);
        setViewStyle(holder);
        holder.editText.setHint(mHint);
        holder.editText.setText(mContentText);
        holder.editText.setChecked(isValid());
    }

    public void setContentText(CharSequence contentText) {
        if (contentText != null) {
            mContentText = contentText.toString();
            if (mAutoTrim) {
                mContentText = mContentText.trim();
            }
        } else {
            mContentText = null;
        }
        setValid(mOnValidateListener != null ? mOnValidateListener.onValidate(this, false) : true);
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
        return mJsonPath;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

    }

    public static class ViewHolder {
        FormEditText editText;
        TextWatcher textWatcher;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }

    public void setError(String errorMsg) {

    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mOnValidateListener != null) {
                mOnValidateListener.onTextChanged(s, start, before, count);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (mOnValidateListener != null) {
                mOnValidateListener.beforeTextChanged(s, start, count, after);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mOnValidateListener != null) {
                mOnValidateListener.afterTextChanged(s);
            }
            mContentText = s.toString();
        }
    };

    private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                setContentText(((TextView) view).getText().toString());
            }
        }
    };

    @Override
    public boolean validate() {
        setContentText(mContentText);
        setValid(mOnValidateListener != null ? mOnValidateListener.onValidate(this, true) : CharSequenceUtils.isEmpty(mContentText)
                || mAllowEmptyContent);
        return isValid();
    }

}
