package com.shopelia.android.utils;

import java.util.ArrayList;
import java.util.List;
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

    public static String getCountryDisplayName(String iso) {
        return new Locale("", iso).getDisplayCountry();
    }

    public static List<String> getCountries() {
        Locale[] locales = Locale.getAvailableLocales();
        List<String> countries = new ArrayList<String>(locales.length);
        for (Locale locale : locales) {
            if (!countries.contains(locale.getDisplayCountry())) {
                countries.add(locale.getDisplayCountry());
            }
        }
        return countries;
    }

}
