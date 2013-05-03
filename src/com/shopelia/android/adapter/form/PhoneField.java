package com.shopelia.android.adapter.form;

import java.util.regex.Pattern;

import android.content.Context;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;

public class PhoneField extends EditTextField {

    public static final int TYPE = 3;

    public static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,13}$");

    private PhoneNumberFormattingTextWatcher mPhoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher();

    public PhoneField(String defaultText, String hint) {
        super(defaultText, hint);
    }

    public PhoneField(Context context, String defaultText, int hintResId) {
        super(context, defaultText, hintResId);
    }

    @Override
    protected void setViewStyle(ViewHolder holder) {
        super.setViewStyle(holder);
        holder.editText.setInputType(InputType.TYPE_CLASS_PHONE);
        holder.editText.addTextChangedListener(mPhoneNumberFormattingTextWatcher);
    }

    @Override
    public int getFieldType() {
        return TYPE;
    }

    @Override
    protected boolean onValidation(boolean fireError) {
        String content = (String) getResult();
        boolean out = super.onValidation(fireError) && PHONE_PATTERN.matcher(content).matches();
        if (getBoundedView() != null) {
            bindView(getBoundedView());
        }
        return out;
    }

    @Override
    public Object getResult() {
        return super.getResult() != null ? ((String) super.getResult()).replace(" ", "") : "";
    }

}
