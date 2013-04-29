package com.shopelia.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.shopelia.android.R;

public class ShopeliaCheckableButton extends CheckBox {

    private static final int[] ERROR_STATE_SET = {
        R.attr.state_error
    };

    private boolean mError = false;

    public ShopeliaCheckableButton(Context context) {
        super(context);
    }

    public ShopeliaCheckableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShopeliaCheckableButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setError(boolean hasError) {
        if (hasError() != hasError) {
            mError = hasError;
            invalidate();
        }
    }

    public boolean hasError() {
        return mError;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if (hasError()) {
            mergeDrawableStates(drawableState, ERROR_STATE_SET);
        }

        return drawableState;
    }

}
