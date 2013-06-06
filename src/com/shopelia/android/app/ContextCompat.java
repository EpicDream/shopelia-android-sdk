package com.shopelia.android.app;

import java.io.File;
import java.io.IOException;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

/**
 * An extension of the ContextCompat class from the support library.
 * 
 * @author Cyril Mottier
 */
public final class ContextCompat extends android.support.v4.content.ContextCompat {

    private static final String LOG_TAG = "ContextCompat";

    /**
     * Interface for the full API.
     */
    private interface ContextVersionImpl {
        File getExternalCacheDir(Context context);
    }

    /**
     * @author Cyril Mottier
     */
    private static class BaseContextCompat implements ContextVersionImpl {

        private File mExternalCacheDir;

        private static final File EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY = new File(new File(Environment.getExternalStorageDirectory(),
                "Android"), "data");

        @Override
        public File getExternalCacheDir(Context context) {
            if (mExternalCacheDir == null) {
                mExternalCacheDir = new File(new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY, context.getPackageName()), "cache");
            }
            if (!mExternalCacheDir.exists()) {
                try {
                    (new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY, ".nomedia")).createNewFile();
                } catch (IOException e) {
                }
                if (!mExternalCacheDir.mkdirs()) {
                    Log.w(LOG_TAG, "Unable to create external cache directory");
                    return null;
                }
            }
            return mExternalCacheDir;
        }
    }

    /**
     * @author Cyril Mottier
     */
    private static class FroyoContextCompat implements ContextVersionImpl {
        @TargetApi(8)
        @Override
        public File getExternalCacheDir(Context context) {
            return context.getExternalCacheDir();
        }
    }

    private static final ContextVersionImpl IMPL;
    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            IMPL = new FroyoContextCompat();
        } else {
            IMPL = new BaseContextCompat();
        }
    }

    private ContextCompat() {
    }

    /**
     * Returns the absolute path to the directory on the external filesystem
     * (that is somewhere on {@link Environment#getExternalStorageDirectory()}
     * where the application can place cache files it owns.
     * 
     * @param context
     * @see Context#getExternalCacheDir()
     * @return Returns the path of the directory holding application cache files
     *         on external storage. Returns null if external storage is not
     *         currently mounted so it could not ensure the path exists; you
     *         will need to call this method again when it is available.
     */
    public static File getExternalCacheDir(Context context) {
        return IMPL.getExternalCacheDir(context);
    }

}
