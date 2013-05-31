package com.shopelia.android.utils;

/**
 * Utility class used for format strings
 * 
 * @author Pierre Pollastri
 */
public final class FormatUtils {

    private FormatUtils() {

    }

    /**
     * Truncate float price to cents.
     * 
     * @param price
     * @return The price in a String truncated at cents
     */
    public static String formatPrice(float price) {
        int intPrice = (int) (price * 100);
        return String.valueOf(intPrice / 100) + "." + String.valueOf(price % 100);
    }
}
