package com.shopelia.android.adapter.form;

import android.content.Context;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.text.TextUtils;

public class PhoneField extends EditTextField {

    public static final int TYPE = 3;

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
        holder.editText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
    }

    public CharSequence makePrettyPhoneNumber(CharSequence source) {
        if (TextUtils.isEmpty(source)) {
            return source;
        }
        StringBuilder builder = new StringBuilder();
        source = makeUglyPhoneNumber(source);
        int cluster = 2;
        if (!TextUtils.isDigitsOnly(source.subSequence(0, 1).toString())) {
            cluster = 3;
        }
        final int count = source.length();
        for (int index = 0; index < count; index++) {
            if (index > 0 && index % cluster == 0) {
                builder.append(' ');
            }
            builder.append(source.charAt(index));
        }
        return builder;
    }

    public CharSequence makeUglyPhoneNumber(CharSequence source) {
        if (TextUtils.isEmpty(source)) {
            return source;
        }
        return source.toString().replace(" ", "");
    }

    @Override
    public int getFieldType() {
        return TYPE;
    }

}
