package com.shopelia.android.graphics;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Build;

/**
 * @author Cyril Mottier
 */
public final class BitmapCompat {

    /**
     * Interface for the full API.
     */
    private interface BitmapVersionImpl {
        public int getByteCount(Bitmap bitmap);
    }

    private static class BaseBitmapVersionImpl implements BitmapVersionImpl {

        @Override
        public int getByteCount(Bitmap bitmap) {

            final int bytePerPixel;
            final Config config = bitmap.getConfig();
            if (config == null) {
                // The config is null. It's probably because the underlying
                // Bitmap is using a non-public format. As a result, our best
                // option is to consider the worst case scenario (4 bytes per
                // pixel)
                bytePerPixel = 4;
            } else {
                switch (config) {
                    case ALPHA_8:
                        // 1 byte per pixel
                        bytePerPixel = 1;
                        break;

                    case ARGB_4444:
                    case RGB_565:
                        // 2 bytes per pixel
                        bytePerPixel = 2;
                        break;

                    case ARGB_8888:
                    default:
                        // 4 bytes per pixel
                        bytePerPixel = 4;
                        break;
                }
            }

            return bitmap.getHeight() * bitmap.getWidth() * bytePerPixel;
        }
    }

    /**
     * Interface implementation for devices with at least v12 APIs.
     */
    private static class HoneycombMR1BitmapVersionImpl implements BitmapVersionImpl {
        @TargetApi(12)
        @Override
        public int getByteCount(Bitmap bitmap) {
            return bitmap.getByteCount();
        }
    }

    private static final BitmapVersionImpl IMPL;
    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            IMPL = new HoneycombMR1BitmapVersionImpl();
        } else {
            IMPL = new BaseBitmapVersionImpl();
        }
    }

    private BitmapCompat() {
    }

    /**
     * Returns the number of bytes used to store the given bitmap's pixels.
     * 
     * @param bitmap A Bitmap
     * @return The number of bytes used to store the given Bitmap
     */
    public static int getByteCount(Bitmap bitmap) {
        return IMPL.getByteCount(bitmap);
    }

}
