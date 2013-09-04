package com.shopelia.android.widget;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
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

    public static final String ASAP_BOLD = "asap_bold.otf";
    public static final String ASAP_BOLD_ITALIC = "asap_bold_italic.otf";
    public static final String ASAP_ITALIC = "asap_italic.otf";
    public static final String ASAP_REGULAR = "asap_regular.ttf";

    private static final SparseArray<SparseArray<Typeface>> sTypefaces = new SparseArray<SparseArray<Typeface>>(3);

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
     * Gets thes best typeface for the given family & style.
     * 
     * @param fontFamily
     * @param fontStyle
     * @return
     */
    private Typeface getTypeface(int fontFamily, int fontStyle) {

        if (isInEditMode() || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) {
            switch (fontStyle) {
                case STYLE_BOLD:
                    return Typeface.DEFAULT_BOLD;
                default:
                    return Typeface.DEFAULT;
            }
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
                        typeface = tryCreateTypefaceFromAsset(getContext(), ASAP_REGULAR, R.raw.shopelia_fonts_asap_regular);
                        break;

                    default:
                    case STYLE_BOLD:
                        typeface = tryCreateTypefaceFromAsset(getContext(), ASAP_BOLD, R.raw.shopelia_fonts_asap_bold);
                        break;
                    case STYLE_ITALIC:
                        typeface = tryCreateTypefaceFromAsset(getContext(), ASAP_ITALIC, R.raw.shopelia_fonts_asap_italic);
                        break;
                    case STYLE_BOLD_ITALIC:
                        typeface = tryCreateTypefaceFromAsset(getContext(), ASAP_BOLD_ITALIC, R.raw.shopelia_fonts_asap_bold_italic);
                        break;
                }
                break;
            }

            case FAMILY_LIGHT: {
                final AssetManager assetManager = getContext().getAssets();
                switch (fontStyle) {
                    case STYLE_NORMAL:
                        typeface = tryCreateTypefaceFromAsset(getContext(), ASAP_REGULAR, R.raw.shopelia_fonts_asap_regular);
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
                        typeface = tryCreateTypefaceFromAsset(getContext(), ASAP_REGULAR, R.raw.shopelia_fonts_asap_regular);
                        break;

                    case STYLE_BOLD_ITALIC:
                        typeface = tryCreateTypefaceFromAsset(getContext(), ASAP_BOLD_ITALIC, R.raw.shopelia_fonts_asap_italic);
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

    public static Typeface tryCreateTypefaceFromAsset(Context context, String path, int resId) {
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
}
