package com.shopelia.android.test.algorithm;

import junit.framework.TestCase;

import com.shopelia.android.algorithm.Luhn;

public class LuhnTest extends TestCase {

    public void testFakeNumber() {
        assertEquals(false, Luhn.isValid("1234567890123456"));
    }

    public void testValidNumber() {
        assertEquals(true, Luhn.isValid("1234567890123452"));
    }

}
