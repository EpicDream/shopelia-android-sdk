package com.shopelia.android.widget;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.SparseArray;

import com.shopelia.android.R;
import com.shopelia.android.config.Config;
import com.shopelia.android.utils.IOUtils;

final class CustomFontHelper {

    public static final int FAMILY_NORMAL = 0;
    public static final int FAMILY_CONDENSED = 1;
    public static final int FAMILY_LIGHT = 2;

    public static final int STYLE_NORMAL = Typeface.NORMAL;
    public static final int STYLE_BOLD = Typeface.BOLD;
    public static final int STYLE_ITALIC = Typeface.ITALIC;
    public static final int STYLE_BOLD_ITALIC = Typeface.BOLD_ITALIC;

    private static final SparseArray<SparseArray<Typeface>> sTypefaces = new SparseArray<SparseArray<Typeface>>(3);

    private static final Font[] FONTS;

    static {
        // Add fonts here
        FONTS = new Font[] {
                new Font(FAMILY_NORMAL, STYLE_NORMAL, R.raw.shopelia_helvetica_neue),
                new Font(FAMILY_NORMAL, STYLE_BOLD, R.raw.shopelia_helvetica_neue_bold),
                new Font(FAMILY_LIGHT, STYLE_NORMAL, R.raw.shopelia_helvetica_light)
        };
    }

    private CustomFontHelper() {

    }

    /**
     * Gets the best typeface for the given family & style.
     * 
     * @param fontFamily
     * @param fontStyle
     * @return
     */
    public static Typeface getTypeface(Context context, int fontFamily, int fontStyle) {

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) {
            return fontStyle == STYLE_BOLD ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT;
        }

        SparseArray<Typeface> typefacesForFamily = sTypefaces.get(fontFamily);
        if (typefacesForFamily != null) {
            final Typeface typeface = typefacesForFamily.get(fontStyle);
            if (typeface != null) {
                // Cache hit
                return typeface;
            }
        }

        Typeface typeface = null;

        // There has been a cache miss. Let's load the appropriate typeface

        int bestMatch = 0;
        Font bestMatchFont = null;

        for (Font font : FONTS) {
            int match = font.match(fontFamily, fontStyle);
            if (match > bestMatch) {
                bestMatchFont = font;
            }
        }

        if (bestMatchFont != null) {
            typeface = tryCreateTypefaceFromSharedDirectory(context, bestMatchFont.getFilename(), bestMatchFont.getResId());
        }

        if (typeface == null) {
            typeface = Typeface.DEFAULT;
        } else {
            if (typefacesForFamily == null) {
                typefacesForFamily = new SparseArray<Typeface>();
                sTypefaces.put(fontFamily, typefacesForFamily);
            }
            typefacesForFamily.put(fontStyle, typeface);
        }

        return typeface;
    }

    public static Typeface tryCreateTypefaceFromSharedDirectory(Context context, String path, int resId) {
        File fontDir = new File(Environment.getExternalStorageDirectory(), Config.PUBLIC_FONTS_DIRECTORY);
        fontDir.mkdirs();
        File fontFile = new File(fontDir, path);
        try {
            InputStream is = context.getResources().openRawResource(resId);
            FileOutputStream fos = new FileOutputStream(fontFile);
            BufferedOutputStream os = new BufferedOutputStream(fos);
            IOUtils.copy(is, os);
            os.flush();
            is.close();
            os.close();
            return Typeface.createFromFile(new File(fontDir, path));
        } catch (Exception e) {
            e.printStackTrace();
            fontFile.delete();
            return Typeface.DEFAULT;
        }
    }

    private static class Font {

        private final int mResId;
        private final int mFamily;
        private final int mStyle;

        public Font(int family, int style, int resId) {
            mFamily = family;
            mStyle = style;
            mResId = resId;
        }

        public int match(int family, int style) {
            return 2 * matchIntegers(family, mFamily) + matchIntegers(style, mStyle);
        }

        private int matchIntegers(int i1, int i2) {
            return i1 == i2 ? 1 : 0;
        }

        @SuppressLint("DefaultLocale")
        public String getFilename() {
            return String.format("shopelia_font_%d_%d", mFamily, mStyle);
        }

        public int getResId() {
            return mResId;
        }

    }

}
