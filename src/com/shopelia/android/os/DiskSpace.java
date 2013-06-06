package com.shopelia.android.os;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

/**
 * A simple wrapper around {@link StatFs} that can be used to retrieve the
 * available and total storage space on the phone.
 * 
 * @author Cyril Mottier
 */
public class DiskSpace {

    private Context mContext;

    /**
     * Creates a new {@link DiskSpace} for the given {@link Context}
     * 
     * @param context The {@link Context}
     */
    public DiskSpace(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Return the available storage space on the phone's internal memory.
     * 
     * @return The available storage space on the phone's internal memory
     */
    public long getInternalStorageUsableSpace() {
        return getStorageUsableSpace(mContext.getFilesDir());
    }

    /**
     * Return the total storage space on the phone's internal memory.
     * 
     * @return The total storage space on the phone's internal memory
     */
    public long getInternalStorageTotalSpace() {
        return getStorageTotalSpace(mContext.getFilesDir());
    }

    /**
     * Return the available storage space on the phone's external storage. This
     * method returns 0 if the external storage is not mounted.
     * 
     * @return The available storage space on the phone's external storage.
     * @see Environment
     */
    public long getExternalStorageUsableSpace() {
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return getStorageUsableSpace(Environment.getExternalStorageDirectory());
        }
        return 0;
    }

    /**
     * Return the total storage space on the phone's external storage. This
     * method returns 0 if the external storage is not mounted.
     * 
     * @return The total storage space on the phone's external storage.
     * @see Environment
     */
    public long getExternalStorageTotalSpace() {
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return getStorageTotalSpace(Environment.getExternalStorageDirectory());
        }
        return 0;
    }

    private long getStorageUsableSpace(File file) {
        final StatFs stat = new StatFs(file.getPath());
        return (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
    }

    private long getStorageTotalSpace(File file) {
        final StatFs stat = new StatFs(file.getPath());
        return (long) stat.getBlockCount() * (long) stat.getBlockSize();
    }

}
