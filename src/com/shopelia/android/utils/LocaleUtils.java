package com.shopelia.android.utils;

import java.util.Locale;

public final class LocaleUtils {

    private LocaleUtils() {

    }

    public static String getCountryISO2Code(String displayName) {
        if (displayName == null) {
            return null;
        }
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getDisplayCountry().equalsIgnoreCase(displayName)) {
                return locale.getCountry();
            }
        }
        return null;
    }
}
