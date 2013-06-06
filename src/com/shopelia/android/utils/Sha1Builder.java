package com.shopelia.android.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A simple class that can be used to easily build a SHA-1.
 * 
 * @author Cyril Mottier
 */
public class Sha1Builder {

    private final StringBuilder mBuilder = new StringBuilder();
    private final MessageDigest mDigest;
    private volatile String mSha1;

    public Sha1Builder() {
        try {
            mDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            // HACK Cyril: No exception handling. However, Java specifications
            // mentions the SHA-1 MessageDigest's algorithm is always available.
            // (see http://docs.oracle.com/javase/6/docs/technotes/guides/
            // security/StandardNames.html#MessageDigest)
            throw new RuntimeException("Unable to find the SHA-1 MessageDigest");
        }
    }

    public void reset() {
        mDigest.reset();
        mSha1 = null;
        mBuilder.setLength(0);
    }

    public void update(String input) {
        update(input.getBytes());
    }

    public void update(byte[] input) {
        mDigest.update(input);
    }

    public void update(byte[] input, int offset, int len) {
        mDigest.update(input, offset, len);
    }

    public String build() {
        if (mSha1 == null) {
            final byte[] digest = mDigest.digest();
            final StringBuilder builder = mBuilder;
            for (int i = 0; i < digest.length; i++) {
                final int b = digest[i] & 255;
                if (b < 16) {
                    builder.append('0');
                }
                builder.append(Integer.toHexString(b));
            }

            mSha1 = builder.toString();
        }

        return mSha1;
    }
}
