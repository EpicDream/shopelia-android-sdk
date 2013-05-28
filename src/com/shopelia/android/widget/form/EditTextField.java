package com.shopelia.android.widget.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.shopelia.android.R;
import com.shopelia.android.adapter.FormAdapter;
import com.shopelia.android.widget.Errorable;
import com.shopelia.android.widget.FormEditText;

public class EditTextField extends FormField implements Errorable {

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
    public static final int INVALID_LENGTH = -1;

    public static final String SAVE_TAG = "EditTextFieldSave_";

    private String mContentText;
    private String mHint;
    private OnValidateListener mOnValidateListener;
    private boolean mAllowEmptyContent = true;
    private boolean mAutoTrim = true;
    private String mJsonPath;

    private int mMaxLength = INVALID_LENGTH;

    private boolean mError = false;

    private EditText mBoundedEditText;

    public EditTextField(Context context) {
        this(context, null);
    }

    public EditTextField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.EditTextField, 0, 0);
            try {
                mContentText = ta.getString(R.styleable.EditTextField_shopelia_text);
                mHint = ta.getString(R.styleable.EditTextField_shopelia_hint);
                mMaxLength = ta.getInt(R.styleable.EditTextField_shopelia_max_length, INVALID_LENGTH);
            } finally {
                ta.recycle();
            }
        }
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
        mBoundedEditText = holder.editText;
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.editText.removeTextChangedListener(holder.textWatcher);
        holder.textWatcher = mTextWatcher;
        holder.editText.setOnFocusChangeListener(mOnFocusChangeListener);
        mBoundedEditText = holder.editText;
        setViewStyle(holder);
        holder.editText.setHint(mHint);
        holder.editText.setText(mContentText);
        holder.editText.setOnEditorActionListener(mOnEditorActionListener);
        holder.editText.setChecked(isValid());
        holder.editText.setError(hasError());
        holder.boundedField = this;
        if (!isInEditMode()) {
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
        if (mMaxLength > INVALID_LENGTH && mBoundedEditText != null) {
            mBoundedEditText.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(mMaxLength)
            });
        }
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
        outState.putString(SAVE_TAG + getJsonPath(), mContentText);
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
                if (getFormContainer() != null) {
                    getFormContainer().requestFocus(EditTextField.this);
                }
            }
        }
    };

    private OnEditorActionListener mOnEditorActionListener = new OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return getFormContainer().nextField(EditTextField.this);
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

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            mBoundedEditText.requestFocus();
        }
    }

    public boolean onValidation(boolean fireError) {
        return (!TextUtils.isEmpty(mContentText) || mAllowEmptyContent);
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

    public View getView() {
        return mBoundedEditText;
    }

}
