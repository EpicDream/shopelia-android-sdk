package com.shopelia.android.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * A lightweight version of Common IO by Apache. Inspired by Apache Common IO
 * 
 * @author Pierre Pollastri
 */
public final class IOUtils {

    private static final int BYTE_BUFFER_SIZE = 4096;
    private static final int CHAR_BUFFER_SIZE = 2048;

    private IOUtils() {

    }

    public static void closeQuietly(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
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
        byte[] buffer = new byte[BYTE_BUFFER_SIZE];
        int readBytes = 0;
        while ((readBytes = is.read(buffer)) > 0) {
            os.write(buffer, 0, readBytes);
        }
    }

    public static void copy(InputStream is, Writer writer, Charset charset) throws IOException {
        InputStreamReader reader = new InputStreamReader(is, charset);
        copy(reader, writer);
    }

    public static void copy(Reader reader, OutputStream os, Charset charset) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(os, charset);
        copy(reader, writer);
        writer.flush();
    }

    public static void copy(Reader reader, Writer writer) throws IOException {
        char[] buffer = new char[CHAR_BUFFER_SIZE];
        int readChars = 0;
        while ((readChars = reader.read(buffer)) > 0) {
            writer.write(buffer, 0, readChars);
        }
    }

}
