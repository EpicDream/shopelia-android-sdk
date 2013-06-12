package com.shopelia.android.text.method;

import java.util.HashSet;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

public class ObfuscationTransformationMethod extends PasswordTransformationMethod {

    public static final char DOT = '\u2022';

    private char mSubstitute = DOT;
    private int mLength = 0;
    private HashSet<Character> mExclude = new HashSet<Character>();

    public ObfuscationTransformationMethod() {
        super();
    }

    public ObfuscationTransformationMethod(ObfuscationTransformationMethod cpy) {
        this(cpy.mLength, cpy.mSubstitute);
    }

    public ObfuscationTransformationMethod(int length, char obfuscationCharacter, char... excludes) {
        this();
        setLenghtToObfuscate(length);
        setSubstitute(obfuscationCharacter);
        setExcludeList(excludes);
    }

    public void setSubstitute(char c) {
        mSubstitute = c;
    }

    public void setLenghtToObfuscate(int length) {
        mLength = length;
    }

    public int getLenghtToObfuscate() {
        return mLength;
    }

    public void doNotObfuscate(char c) {
        mExclude.add(Character.valueOf(c));
    }

    public void setExcludeList(char[] cs) {
        mExclude.clear();
        for (int index = 0; index < cs.length; index++) {
            mExclude.add(Character.valueOf(cs[index]));
        }
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
