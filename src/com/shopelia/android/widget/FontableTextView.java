package com.shopelia.android.widget;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.TextView;

import com.shopelia.android.R;

/**
 * @author Cyril Mottier
 */
public class FontableTextView extends TextView {

    public static final int FAMILY_NORMAL = 0;
    public static final int FAMILY_CONDENSED = 1;
    public static final int FAMILY_LIGHT = 2;

    public static final int STYLE_NORMAL = Typeface.NORMAL;
    public static final int STYLE_BOLD = Typeface.BOLD;
    public static final int STYLE_ITALIC = Typeface.ITALIC;
    public static final int STYLE_BOLD_ITALIC = Typeface.BOLD_ITALIC;

    private static final String FONT_PATH = "shopelia/style/fonts/";

    @SuppressWarnings("unused")
    public static final String ASAP_BOLD = FONT_PATH + "Asap-Bold.otf";
    public static final String ASAP_BOLD_ITALIC = FONT_PATH + "Asap-BoldItalic.otf";
    @SuppressWarnings("unused")
    public static final String ASAP_ITALIC = FONT_PATH + "Asap-Italic.otf";
    public static final String ASAP_REGULAR = FONT_PATH + "Asap-Regular.ttf";

    private static final SparseArray<SparseArray<Typeface>> sTypefaces = new SparseArray<SparseArray<Typeface>>(3);

    public FontableTextView(Context context) {
        this(context, null);
    }

    public FontableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontableTextView, defStyle, 0);

        final int fontFamily = a.getInt(R.styleable.FontableTextView_fontFamily, FAMILY_NORMAL);
        final int fontStyle = a.getInt(R.styleable.FontableTextView_fontStyle, STYLE_NORMAL);

        setTypeface(getTypeface(fontFamily, fontStyle));

        a.recycle();
    }

    /**
     * Gets thes best typeface for the given family & style.
     * 
     * @param fontFamily
     * @param fontStyle
     * @return
     */
    private Typeface getTypeface(int fontFamily, int fontStyle) {

        if (isInEditMode()) {
            return Typeface.DEFAULT;
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
        switch (fontFamily) {
            default:
            case FAMILY_NORMAL: {
                final AssetManager assetManager = getContext().getAssets();
                switch (fontStyle) {
                    case STYLE_NORMAL:
                        typeface = tryCreateTypefaceFromAsset(assetManager, ASAP_REGULAR);
                        break;

                    default:
                    case STYLE_BOLD:
                        typeface = tryCreateTypefaceFromAsset(assetManager, ASAP_BOLD);
                        break;
                    case STYLE_ITALIC:
                        typeface = tryCreateTypefaceFromAsset(assetManager, ASAP_ITALIC);
                        break;
                    case STYLE_BOLD_ITALIC:
                        typeface = tryCreateTypefaceFromAsset(assetManager, ASAP_BOLD_ITALIC);
                        break;
                }
                break;
            }

            case FAMILY_LIGHT: {
                final AssetManager assetManager = getContext().getAssets();
                switch (fontStyle) {
                    case STYLE_NORMAL:
                        typeface = tryCreateTypefaceFromAsset(assetManager, ASAP_REGULAR);
                        break;

                    default:
                    case STYLE_BOLD:
                    case STYLE_ITALIC:
                    case STYLE_BOLD_ITALIC:
                        // These fonts are not used for now in the
                        // application. Let's fallback to the default font
                        // instead. We'll add them to the application only if
                        // necessary
                        break;
                }
                break;
            }

            case FAMILY_CONDENSED: {
                final AssetManager assetManager = getContext().getAssets();
                switch (fontStyle) {
                    case STYLE_BOLD:
                        typeface = tryCreateTypefaceFromAsset(assetManager, ASAP_REGULAR);
                        break;

                    case STYLE_BOLD_ITALIC:
                        typeface = tryCreateTypefaceFromAsset(assetManager, ASAP_BOLD_ITALIC);
                        break;

                    default:
                    case STYLE_NORMAL:
                    case STYLE_ITALIC:
                        // These fonts are not used for now in the
                        // application. Let's fallback to the default font
                        // instead. We'll add them to the application only if
                        // necessary
                        break;
                }
                break;
            }
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

    public static Typeface tryCreateTypefaceFromAsset(AssetManager assetManager, String path) {
        try {
            return Typeface.createFromAsset(assetManager, path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
