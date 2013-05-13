package com.shopelia.android.adapter.form;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.shopelia.android.R;
import com.shopelia.android.adapter.FormAdapter;
import com.shopelia.android.adapter.FormAdapter.Field;
import com.shopelia.android.widget.Errorable;
import com.shopelia.android.widget.FormEditText;

public class EditTextField extends Field implements Errorable {

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
    public static final String SAVE_TAG = "EditTextFieldSave_";

    private String mContentText;
    private String mHint;
    private OnValidateListener mOnValidateListener;
    private boolean mAllowEmptyContent = true;
    private boolean mAutoTrim = true;
    private String mJsonPath;

    private boolean mError = false;

    private EditText mBoundedEditText;

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

    /**
     * Sets this field as mandatory. It has to be filled to success validation
     * 
     * @return
     */
    public EditTextField mandatory() {
        mAllowEmptyContent = false;
        return this;
    }

    /**
     * Sets this field as optional. It will not be necessary to be filled to
     * success validation.
     * 
     * @return
     */
    public EditTextField optional() {
        mAllowEmptyContent = true;
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
        holder.editText.setOnFocusChangeListener(mOnFocusChangeListener);
        setViewStyle(holder);
        holder.editText.setHint(mHint);
        holder.editText.setText(mContentText);
        holder.editText.setOnEditorActionListener(mOnEditorActionListener);
        holder.editText.setChecked(isValid());
        holder.editText.setError(hasError());
        if (holder.boundedField != null) {
            holder.boundedField.mBoundedEditText = null;
        }
        holder.boundedField = this;
        mBoundedEditText = holder.editText;
        if (mTextWatcher != null) {
            holder.editText.addTextChangedListener(mTextWatcher);
        }
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
        setValid(onValidation(false) && (mOnValidateListener != null ? mOnValidateListener.onValidate(this, false) : true));
    }

    protected void setViewStyle(ViewHolder holder) {
        // Here for extension purpose so we can later create PasswordField,
        // EmailField... just by extending EditTextField and overriding this
        // method.
    }

    @Override
    public void onNextField() {
        super.onNextField();
        if (getBoundedView() != null) {
            ViewHolder holder = (ViewHolder) getBoundedView().getTag();
            holder.editText.requestFocus();
        }
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
        outState.putString(SAVE_TAG + mJsonPath, mContentText);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null && mJsonPath != null) {
            setContentText(savedInstanceState.getString(SAVE_TAG + mJsonPath));
        }
    }

    public static class ViewHolder {
        FormEditText editText;
        TextWatcher textWatcher;
        EditTextField boundedField;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }

    public void setError(String errorMsg) {

    }

    protected TextWatcher mTextWatcher = new TextWatcher() {

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
            mContentText = s.toString();
            if (mOnValidateListener != null) {
                mOnValidateListener.afterTextChanged(s);
            }
            setValid(onValidation(false));
            if (getBoundedView() != null) {
                ViewHolder holder = (ViewHolder) getBoundedView().getTag();
                holder.editText.setChecked(isValid());
            }
        }
    };

    private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                setContentText(((TextView) view).getText().toString());
                FormEditText editText = (FormEditText) view;
                if (isValid()) {
                    editText.setChecked(true);
                } else {
                    editText.setChecked(false);
                }
            } else {
                getAdapter().requestFocus(EditTextField.this);
            }
        }
    };

    private OnEditorActionListener mOnEditorActionListener = new OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return getAdapter().nextField(EditTextField.this);
        }
    };

    @Override
    public boolean validate() {
        if (mBoundedEditText != null) {
            mContentText = mBoundedEditText.getText().toString();
        }
        setContentText(mContentText);
        setValid(onValidation(false) && (mOnValidateListener != null ? mOnValidateListener.onValidate(this, true) : true));
        setError(!isValid());
        return isValid();
    }

    @Override
    public void setValid(boolean isValid) {
        super.setValid(isValid);
        if (isValid()) {
            mError = false;
        }
    }

    protected boolean onValidation(boolean fireError) {
        return (!TextUtils.isEmpty(mContentText) || mAllowEmptyContent);
    }

    @Override
    public void notifyDataChanged(View view) {
        super.notifyDataChanged(view);

    }

    @Override
    public void setError(boolean hasError) {
        if (hasError() != hasError) {
            mError = hasError;
            if (getBoundedView() != null) {
                bindView(getBoundedView());
            }
        }
    }

    @Override
    public boolean hasError() {
        return mError;
    }

}
