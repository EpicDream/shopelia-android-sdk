package com.shopelia.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;
import android.widget.Checkable;

import com.shopelia.android.R;

public class FormAutocompleteEditText extends AutoCompleteTextView implements Checkable, Errorable {

    private boolean mChecked = false;
    private boolean mError = false;

    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };

    private static final int[] ERROR_STATE_SET = {
        R.attr.state_error
    };

    public FormAutocompleteEditText(Context context) {
        this(context, null);
    }

    public FormAutocompleteEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FormAutocompleteEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontableTextView, defStyle, 0);

            final int fontFamily = a.getInt(R.styleable.FormEditText_shopelia_fontFamily, CustomFontHelper.FAMILY_NORMAL);
            final int fontStyle = a.getInt(R.styleable.FormEditText_shopelia_fontStyle, CustomFontHelper.STYLE_NORMAL);
            if (!isInEditMode()) {
                setTypeface(CustomFontHelper.getTypeface(getContext(), fontFamily, fontStyle));
            }

            a.recycle();
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (checked != mChecked) {
            mChecked = checked;
            if (checked) {
                setError(false);
            }
            refreshDrawableState();
        }
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public void setError(boolean hasError) {
        if (hasError() != hasError) {
            mError = hasError;
            if (hasError) {
                setChecked(false);
            }
            refreshDrawableState();
        }

    }

    @Override
    public boolean hasError() {
        return mError;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        } else if (hasError()) {
            mergeDrawableStates(drawableState, ERROR_STATE_SET);
        }

        return drawableState;
    }
}
