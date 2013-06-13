package com.shopelia.android.pretty;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;

public class CardNumberFormattingTextWatcher implements TextWatcher {

    private boolean mIsEditing = false;
    private boolean mIsBackwardDeleting = false;
    private int mBackwardDeleteStart = 0;

    @Override
    public synchronized void afterTextChanged(Editable s) {
        if (!mIsEditing) {
            mIsEditing = true;
            makePretty(s);
            mIsEditing = false;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!mIsEditing) {
            if (count == 1 && after == 0 && s.charAt(start) == ' ') {
                mIsBackwardDeleting = true;
                mBackwardDeleteStart = start;
            } else {
                mIsBackwardDeleting = false;
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    private void makePretty(Editable s) {
        for (int index = 0; index < s.length(); index++) {
            if (has4DigitBehind(s, index) && s.charAt(index) != ' ') {
                s.insert(index, " ");
                index++;
            }
            if (s.charAt(index) == ' ' && !has4DigitBehind(s, index) || index >= CardNumberInputFilter.FORMAT.length()
                    || (mIsBackwardDeleting && index == mBackwardDeleteStart - 1)) {
                s.delete(index, index + 1);
                mIsBackwardDeleting = false;
                index--;
                continue;
            }
        }
    }

    public static CharSequence makeNumberPretty(CharSequence in) {
        StringBuilder out = new StringBuilder(in.toString().replace(" ", ""));
        for (int index = 0; index < out.length(); index++) {
            if (has4DigitBehind(out, index)) {
                out.insert(index, " ");
                index++;
            }
        }
        return out;
    }

    private static boolean has4DigitBehind(CharSequence s, int index) {
        if (index < 4) {
            return false;
        }
        String digits = s.subSequence(index - 4, index).toString();
        return TextUtils.isDigitsOnly(digits);
    }

    public static class CardNumberInputFilter implements InputFilter {

        public static final String FORMAT = "0000-0000-0000-0000";

        @Override
        public CharSequence filter(CharSequence s, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder source = new StringBuilder(s);
            int count = source.length();
            for (int index = 0; index < count; index++) {
                if ((!Character.isDigit(source.charAt(index)) && source.charAt(index) != ' ') || (dstart + index) >= FORMAT.length()) {
                    source.deleteCharAt(index);
                    index--;
                    count--;
                }
            }
            return makeNumberPretty(source);
        }
    }

}
