package com.shopelia.android.algorithm;

/**
 * Implementation of the Fibonnacci's 0, 1, 1, 2, 3, 5, 8, 13...
 * 
 * @author Pierre Pollastri
 */
public class Fibonacci {

    public static long get(int n) {
        if (n == 0) {
            return 0L;
        } else if (n == 1) {
            return 1;
        }

        long first = 0L;
        long second = 1L;
        long sum = 0L;
        n -= 2;
        while (n >= 0) {
            sum = first + second;
            first = second;
            second = sum;
            n -= 1;
        }

        return second;
    }
}
