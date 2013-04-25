package com.shopelia.android.pretty;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

public class DateFormattingTextWatcher implements TextWatcher {

    private boolean mIsEditing = false;
    private boolean mZeroFill = false;
    private boolean mBackwardDelete = false;

    @Override
    public synchronized void afterTextChanged(Editable s) {
        if (!mIsEditing) {
            Log.d(null, "TEXT = " + s.toString());
            mIsEditing = true;
            for (int index = 0; index < s.length(); index++) {
                if (s.charAt(index) == '/' && index != 2) {
                    s.delete(index, index + 1);
                    index--;
                }
            }
            if (mZeroFill) {
                s.insert(0, "0");
            }

            if (s.length() > 2 && s.charAt(2) != '/') {
                s.replace(2, 3, "/");
            }

            if (s.length() == 2 && !mBackwardDelete) {
                s.insert(2, "/");
            }

            if (mBackwardDelete) {
                s.delete(1, 2);
            }
            mIsEditing = false;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (start == 2 && count == 1 && after == 0) {
            mBackwardDelete = true;
        } else {
            mBackwardDelete = false;
        }
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
