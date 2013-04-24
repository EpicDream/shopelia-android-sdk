package com.shopelia.android.pretty;

import android.text.Editable;
import android.text.TextWatcher;

public class DateFormattingTextWatcher implements TextWatcher {

    private boolean mIsEditing = false;
    private boolean mZeroFill = false;

    @Override
    public synchronized void afterTextChanged(Editable s) {
        if (!mIsEditing) {
            mIsEditing = true;
            for (int index = 0; index < s.length(); index++) {
                if (s.charAt(index) == '/' && index != 2) {
                    s.delete(index, index + 1);
                    index--;
                } else if (index == 2 && s.charAt(index) != '/') {
                    s.insert(index, "/");
                    index++;
                }
            }
            if (mZeroFill) {
                s.insert(0, "0");
            }
            if (s.length() == 2) {
                s.append("/");
            }
            mIsEditing = false;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (start == 0 && start < s.length() && s.charAt(start) != '1' && s.charAt(start) != '0') {
            mZeroFill = true;
        } else {
            mZeroFill = false;
        }
    }

}
