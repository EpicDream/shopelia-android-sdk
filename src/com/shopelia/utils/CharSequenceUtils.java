package com.shopelia.utils;

/**
 * Utility class easing {@link CharSequence} manipulation and functionality
 * extensions.
 * 
 * @author Pierre Pollastri
 */
public final class CharSequenceUtils {

    private CharSequenceUtils() {

    }

    /**
     * This method checks if the given {@link CharSequence} is not null nor
     * empty.
     * 
     * @param string
     * @return True if the {@link CharSequence} is null or empty, false
     *         otherwise
     */
    public static boolean isEmpty(CharSequence string) {
        return string == null || string.length() == 0;
    }

}
