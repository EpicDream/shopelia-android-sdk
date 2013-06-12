package com.shopelia.android.text.method;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

public class PaymentCardTransformationMethod extends PasswordTransformationMethod {

    private static char DOT = ' ';// '\u2022';

    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PrivateCharSequence(source);
    }

    private class PrivateCharSequence implements CharSequence {

        private CharSequence mSource;

        public PrivateCharSequence(CharSequence source) {
            mSource = source;
        }

        @Override
        public char charAt(int index) {
            return index >= 15 || mSource.charAt(index) == ' ' ? mSource.charAt(index) : mSource.charAt(index);
        }

        @Override
        public int length() {
            return mSource.length();
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return mSource.subSequence(start, end);
        }

    }

}
