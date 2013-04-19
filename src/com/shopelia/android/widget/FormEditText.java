package com.shopelia.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.EditText;

public class FormEditText extends EditText implements Checkable {

    private boolean mChecked = false;

    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };

    public FormEditText(Context context) {
        super(context);
    }

    public FormEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FormEditText(Context context, AttributeSet attrs, int defStyle) {
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
            refreshDrawableState();
        }
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
}
