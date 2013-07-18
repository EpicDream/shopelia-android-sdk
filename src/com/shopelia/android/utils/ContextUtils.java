package com.shopelia.android.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;

public final class ContextUtils {

    private static final String APPLICATION_NAME = "shopelia-sdk-application-name";

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

    public static ApplicationInfo getApplicationInfo(Context context) {
        if (context == null) {
            return null;
        }
        Context app = context.getApplicationContext();
        try {
            return app.getPackageManager().getApplicationInfo(app.getPackageName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public static Bundle getMetadata(Context context) {
        ApplicationInfo ai = getApplicationInfo(context);
        if (ai != null) {
            return ai.metaData;
        }
        return null;
    }

    public static Object getMetadata(Context context, String key) {
        Bundle metadata = getMetadata(context);
        if (metadata != null) {
            return metadata.get(key);
        }
        return null;
    }

    public static String getMetadataString(Context context, String key, String fallback) {
        String out = (String) getMetadata(context, key);
        return out != null ? out : fallback;
    }

    public static String getApplicationName(Context context, String fallback) {
        if (context == null) {
            return null;
        }
        String appName = getMetadataString(context, APPLICATION_NAME, null);
        if (TextUtils.isEmpty(appName)) {
            int resId = context.getApplicationInfo().labelRes;
            appName = context.getString(resId);
        }
        return TextUtils.isEmpty(appName) ? fallback : appName;
    }

}
