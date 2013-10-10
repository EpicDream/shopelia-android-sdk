package com.shopelia.android.utils;

import android.text.SpannableStringBuilder;
import android.text.Spanned;

public final class TextHelper {

    private TextHelper() {

    }

    public static Spanned formatText(CharSequence format, Object... objects) {
        SpannableStringBuilder builder = new SpannableStringBuilder(format);
        CharSequence replacementSpan = "%@";
        for (Object object : objects) {
            int indexOf = builder.toString().indexOf(replacementSpan.toString());
            if (indexOf == -1) {
                break;
            }
            builder.replace(indexOf, indexOf + replacementSpan.length(), object.toString());
        }
        return builder;
    }

}
