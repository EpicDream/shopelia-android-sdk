package com.shopelia.android.adapter.form;

import java.util.regex.Pattern;

import android.content.Context;
import android.text.InputType;

public class EmailField extends EditTextField {

    /**
     * From
     * http://stackoverflow.com/questions/12947620/email-address-validation-
     * in-android-on-edittext
     */
    private final static Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    public EmailField(String defaultText, String hint) {
        super(defaultText, hint);
    }

    public EmailField(Context context, String defaultText, int hintResId) {
        super(context, defaultText, hintResId);
    }

    @Override
    protected void setViewStyle(ViewHolder holder) {
        super.setViewStyle(holder);
        holder.editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    @Override
    protected boolean onValidation(boolean fireError) {
        boolean out = super.onValidation(fireError) && EMAIL_ADDRESS_PATTERN.matcher((CharSequence) getResult()).matches();
        return out;
    }

}
