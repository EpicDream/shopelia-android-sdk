package com.shopelia.android.widget.form;

import java.util.regex.Pattern;

import android.content.Context;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.util.AttributeSet;

public class PhoneField extends EditTextField {

    public static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,13}$");

    private PhoneNumberFormattingTextWatcher mPhoneNumberFormattingTextWatcher;

    public PhoneField(Context context) {
        this(context, null);
    }

    public PhoneField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhoneField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            mPhoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher();
        }
    }

    @Override
    protected void setViewStyle(ViewHolder holder) {
        super.setViewStyle(holder);
        if (!isInEditMode()) {
            holder.editText.setInputType(InputType.TYPE_CLASS_PHONE);
            if (mPhoneNumberFormattingTextWatcher != null) {
                holder.editText.addTextChangedListener(mPhoneNumberFormattingTextWatcher);
            }
        }
    }

    @Override
    protected boolean onValidation(boolean fireError) {
        String content = (String) getResult();
        boolean out = super.onValidation(fireError) && PHONE_PATTERN.matcher(content).matches();
        return out;
    }

    @Override
    public Object getResult() {
        return super.getResult() != null ? ((String) super.getResult()).replace(" ", "") : "";
    }

}
