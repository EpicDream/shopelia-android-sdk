package com.shopelia.android.algorithm;


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
     * @param id A sequence of digit char with no spaces and no separator.
     * @return True if the number is valid false otherwise
     */
    public static boolean isValid(CharSequence id) {
        int sum = 0;
        for (int index = id.length() - 1; index >= 0; index--) {
            int digit = id.charAt(index) - '0';
            if (index % 2 == 0) {
                sum += digit > 4 ? digit * 2 - 9 : digit * 2;
            } else {
                sum += digit;
            }
        }
        return sum % 10 == 0;
    }

}
