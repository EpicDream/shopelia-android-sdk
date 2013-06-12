package com.shopelia.android.text.method;

import java.util.HashSet;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

public class ObfuscationTransformationMethod extends PasswordTransformationMethod {

    public static final char DOT = '\u2022';

    private char mSubstitute = DOT;
    private int mLength;
    private HashSet<Character> mExclude;

    public void setSubstitute(char c) {
        mSubstitute = c;
    }

    public void setLenghtToObfuscate(int length) {
        mLength = length;
    }

    public void doNotObfuscate(char c) {
        mExclude.add(Character.valueOf(c));
    }

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
            return index >= mLength || mExclude.contains(Character.valueOf(mSource.charAt(index))) ? mSource.charAt(index) : mSubstitute;
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
