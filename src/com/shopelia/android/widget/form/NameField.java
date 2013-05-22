package com.shopelia.android.widget.form;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

public class NameField extends EditTextField {

    public NameField(Context context) {
        this(context, null);
    }

    public NameField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NameField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void setViewStyle(ViewHolder holder) {
        super.setViewStyle(holder);
        holder.editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
    }

}
