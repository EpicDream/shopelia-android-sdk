package com.shopelia.android.widget.form;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.AttributeSet;

public class NumberField extends EditTextField {

    private int mMinLength = 0;

    public NumberField(Context context) {
        this(context, null);
    }

    public NumberField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NumberField setMinLength(int minLength) {
        mMinLength = minLength;
        return this;
    }

    @Override
    protected void setViewStyle(ViewHolder holder) {
        super.setViewStyle(holder);
        holder.editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        holder.editText.setTypeface(Typeface.DEFAULT);
    }

    @Override
    public boolean onValidation(boolean fireError) {
        return getResult() != null && ((String) (getResult())).length() >= mMinLength;
    }

}
