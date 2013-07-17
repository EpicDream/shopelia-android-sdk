package com.shopelia.android.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public final class ContextUtils {

    private ContextUtils() {

    }

    public static boolean hasPermision(Context context, String permission) {
        if (context == null) {
            return false;
        }
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (!hasPermision(context, permission)) {
                return false;
            }
        }
        return true;
    }

}
