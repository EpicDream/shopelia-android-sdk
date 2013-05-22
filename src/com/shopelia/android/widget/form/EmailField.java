package com.shopelia.android.widget.form;

import java.util.regex.Pattern;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

public class EmailField extends EditTextField {

    /**
     * From
     * http://stackoverflow.com/questions/12947620/email-address-validation-
     * in-android-on-edittext
     */
    private final static Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    public EmailField(Context context) {
        this(context, null);
    }

    public EmailField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmailField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
