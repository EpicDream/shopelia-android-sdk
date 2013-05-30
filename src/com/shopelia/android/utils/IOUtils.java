package com.shopelia.android.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A lightweight version of Common IO by Apache
 * 
 * @author Pierre Pollastri
 */
public final class IOUtils {

    private IOUtils() {

    }

    public static void closeQuietly(InputStream is) {
        try {
            is.close();
        } catch (Exception e) {
            // Quiet
        }
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            // Quiet
        }
    }

    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[4096];
        int readBytes = 0;
        while ((readBytes = is.read(buffer)) > 0) {
            os.write(buffer, 0, readBytes);
        }
    }

}
