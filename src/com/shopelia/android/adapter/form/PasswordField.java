package com.shopelia.android.adapter.form;

import android.content.Context;
import android.text.InputType;

public class PasswordField extends EditTextField {

    public PasswordField(String defaultText, String hint) {
        super(defaultText, hint);
    }

    public PasswordField(Context context, String defaultText, int hintResId) {
        super(context, defaultText, hintResId);
    }

    @Override
    protected void setViewStyle(ViewHolder holder) {
        super.setViewStyle(holder);
        holder.editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

}
