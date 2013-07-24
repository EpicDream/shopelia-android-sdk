package com.shopelia.android.utils;

public class TimeUnits {

    public static final long MILISECONDS = 1;
    public static final long SECONDS = 1000 * MILISECONDS;
    public static final long MINUTES = 60 * SECONDS;
    public static final long HOURS = 60 * MINUTES;
    public static final long DAYS = 24 * HOURS;
    /*
     * Not precise units
     */
    public static final long MONTHS = 30 * DAYS;
    public static final long YEARS = 52 * MONTHS;

    private TimeUnits() {

    }

}
