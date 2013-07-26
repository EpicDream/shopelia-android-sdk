package com.shopelia.android.graphics;

import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;

/**
 * A color filter derived from {@link ColorMatrixColorFilter} used to
 * re-colorize a irawable. You just have to idicate the color of a pixel in an
 * image and the color that you want at the end. The filter will colorize every
 * pixel of the image in order to render a clean image.
 * 
 * @author Pierre Pollastri
 */
public class ColorizeColorFilter extends ColorMatrixColorFilter {

    public ColorizeColorFilter(int color, int desiredColor) {
        super(computeMatrixe(color, desiredColor));
    }

    public static float[] computeMatrixe(int initialColor, int desiredColor) {
        float ir = Color.red(initialColor);
        float ig = Color.green(initialColor);
        float ib = Color.blue(initialColor);

        float dr = Color.red(desiredColor);
        float dg = Color.green(desiredColor);
        float db = Color.blue(desiredColor);
        // @formatter:off
        return new float[] {
             dr / ir, 0.f, 0.f, 0.f, 0.f,
             0.f, dg / ig, 0.f, 0.f, 0.f,
             0.f, 0.f, db / ib, 0.f, 0.f,
             0.f, 0.f, 0.f, 1.f, 0.f
        };
    }

}
