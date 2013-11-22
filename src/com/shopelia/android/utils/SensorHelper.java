package com.shopelia.android.utils;

import android.hardware.SensorEvent;

/**
 * Created by pollas_p on 22/11/2013.
 */
public final class SensorHelper {

    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1 << 0;
    public static final int ORIENTATION_REVERSE = 1 << 1;

    private static final int INDEX_X = 0;
    private static final int INDEX_Y = 1;
    private static final int INDEX_Z = 2;

    private static final float ONE_EIGHTY_OVER_PI = 57.29577957855f;

    public SensorHelper() {

    }

    /**
     * Based on http://stackoverflow.com/a/14855607/1209254
     * @param event
     * @return
     */
    public int computeOrientation(SensorEvent event) {
        int orientation = -1;
        final float X = -event.values[INDEX_X];
        final float Y = -event.values[INDEX_Y];
        float Z = -event.values[INDEX_Z];
        float magnitude = X*X + Y*Y;
        // Don't trust the angle if the magnitude is small compared to the y value
        if (magnitude * 4 >= Z*Z) {
            float angle = (float)Math.atan2(-Y, X) * ONE_EIGHTY_OVER_PI;
            orientation = (90 - (int)Math.round(angle)) % 360;
            // normalize to 0 - 359 range
            if (orientation < 0) {
                orientation += 360;
            }
        }
        //^^ thanks to google for that code
        //now we must figure out which orientation based on the degrees
        if(orientation == -1){//basically flat
            return ORIENTATION_PORTRAIT;
        }
        else if(orientation <= 45 || orientation > 315){//round to 0
            return ORIENTATION_PORTRAIT;
        }
        else if(orientation > 45 && orientation <= 135){//round to 90
            return ORIENTATION_LANDSCAPE; //lsleft
        }
        else if(orientation > 135 && orientation <= 225){//round to 180
            return ORIENTATION_PORTRAIT | ORIENTATION_REVERSE; //upside down
        }
        else if(orientation > 225 && orientation <= 315){//round to 270
            return ORIENTATION_LANDSCAPE | ORIENTATION_REVERSE;
        }
        return orientation;
    }

}
