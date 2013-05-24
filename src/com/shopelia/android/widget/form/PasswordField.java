package com.shopelia.android.widget.form;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.AttributeSet;

public class PasswordField extends EditTextField {

    public PasswordField(Context context) {
        this(context, null);
    }

    public PasswordField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void setViewStyle(ViewHolder holder) {
        super.setViewStyle(holder);
        holder.editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        holder.editText.setTypeface(Typeface.DEFAULT);
    }

    @Override
    protected boolean onValidation(boolean fireError) {
        return ((String) (getResult())).length() >= 4;
    }

}
