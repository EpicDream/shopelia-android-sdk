package com.shopelia.android.widget;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Environment;
import android.text.Html;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.TextView;

import com.shopelia.android.R;
import com.shopelia.android.config.Config;
import com.shopelia.android.utils.IOUtils;

/**
 * @author Pierre Pollastri
 */
public class FontableTextView extends TextView {

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

    private boolean mIsHtml;

    public FontableTextView(Context context) {
        this(context, null);
    }

    public FontableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontableTextView, defStyle, 0);

        final int fontFamily = a.getInt(R.styleable.FontableTextView_shopelia_fontFamily, FAMILY_NORMAL);
        final int fontStyle = a.getInt(R.styleable.FontableTextView_shopelia_fontStyle, STYLE_NORMAL);
        final String htmlText = a.getString(R.styleable.FontableTextView_shopelia_htmlText);
        setTypeface(getTypeface(fontFamily, fontStyle));

        if (htmlText != null && !isInEditMode()) {
            setText(Html.fromHtml(htmlText));
        } else if (htmlText != null && isInEditMode()) {
            setText(htmlText);
        }

        a.recycle();
    }

    public boolean isFromHtml() {
        return mIsHtml;
    }

    public void setFromHtml(boolean fromHtml) {
        if (mIsHtml != fromHtml) {
            mIsHtml = fromHtml;
            setText(getText());
        }
    }

    /**
     * Gets the best typeface for the given family & style.
     * 
     * @param fontFamily
     * @param fontStyle
     * @return
     */
    private Typeface getTypeface(int fontFamily, int fontStyle) {

        if (isInEditMode() || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) {
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
            typeface = tryCreateTypefaceFromSharedDirectory(getContext(), bestMatchFont.getFilename(), bestMatchFont.getResId());
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

        public String getFilename() {
            return String.format("shopelia_font_%d_%d", mFamily, mStyle);
        }

        public int getResId() {
            return mResId;
        }

    }

}
