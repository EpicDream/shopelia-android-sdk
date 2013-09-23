package com.shopelia.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

import com.shopelia.android.R;

/**
 * @author Pierre Pollastri
 */
public class FontableTextView extends TextView {

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

        final int fontFamily = a.getInt(R.styleable.FontableTextView_shopelia_fontFamily, CustomFontHelper.FAMILY_NORMAL);
        final int fontStyle = a.getInt(R.styleable.FontableTextView_shopelia_fontStyle, CustomFontHelper.STYLE_NORMAL);
        final String htmlText = a.getString(R.styleable.FontableTextView_shopelia_htmlText);
        if (!isInEditMode()) {
            setTypeface(CustomFontHelper.getTypeface(getContext(), fontFamily, fontStyle));
        }

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
}
