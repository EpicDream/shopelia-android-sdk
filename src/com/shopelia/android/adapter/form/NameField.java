package com.shopelia.android.adapter.form;

import android.content.Context;
import android.text.InputType;

public class NameField extends EditTextField {

    public NameField(String defaultText, String hint) {
        super(defaultText, hint);
    }

    public NameField(Context context, String defaultText, int hintResId) {
        super(context, defaultText, hintResId);
    }

    @Override
    protected void setViewStyle(ViewHolder holder) {
        super.setViewStyle(holder);
        holder.editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
    }

}
