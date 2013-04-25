package com.shopelia.android.algorithm;

import android.util.Log;

/**
 * A helper class to check if a given card number is valid using Luhn Algorithm.<br/>
 * {@link http://www.brainjar.com/js/validation/default2.asp}
 * 
 * @author Pierre Pollastri
 */
public final class Luhn {

    private Luhn() {

    }

    /**
     * Checks number validity using Luhn mod10 algorithm. <b>You must provide a
     * string only composed of digits other this methods will give you an
     * irrelevant result.</b>
     * 
     * @param id
     * @return
     */
    static boolean isValid(CharSequence id) {
        int sum = 0;
        for (int index = id.length() - 1; index >= 0; index--) {
            int digit = id.charAt(index) - '0';
            Log.d(null, "DIGIT = " + digit);
            if (index > 0 && index % 2 == 0) {
                sum += digit > 4 ? digit * 2 - 9 : digit * 2;
            } else {
                sum += digit;
            }
        }
        return sum % 10 == 0;
    }

}
