package com.shopelia.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.Checkable;

import com.shopelia.android.R;

public class FormEditTextButton extends Button implements Checkable, Errorable {

    private boolean mChecked = false;
    private boolean mError = false;

    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };

    private static final int[] ERROR_STATE_SET = {
        R.attr.state_error
    };

    public FormEditTextButton(Context context) {
        super(context);
    }

    public FormEditTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FormEditTextButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
