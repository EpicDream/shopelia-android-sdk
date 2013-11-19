package com.shopelia.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontableTextView, defStyle, 0);

            final int fontFamily = a.getInt(R.styleable.FontableTextView_shopelia_fontFamily, CustomFontHelper.FAMILY_NORMAL);
            final int fontStyle = a.getInt(R.styleable.FontableTextView_shopelia_fontStyle, CustomFontHelper.STYLE_NORMAL);
            if (!isInEditMode()) {
                setTypeface(CustomFontHelper.getTypeface(getContext(), fontFamily, fontStyle));
            }

            a.recycle();
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(fixSpanColor(text), type);
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

    // For Android 4.3 xml parser bug -_-
    private CharSequence fixSpanColor(CharSequence text) {
        if (text instanceof Spanned) {
            final SpannableString s = new SpannableString(text);
            final ForegroundColorSpan[] spans = s.getSpans(0, s.length(), ForegroundColorSpan.class);
            for (final ForegroundColorSpan oldSpan : spans) {
                final ForegroundColorSpan newSpan = new ForegroundColorSpan(0xFF000000 | oldSpan.getForegroundColor());
                s.setSpan(newSpan, s.getSpanStart(oldSpan), s.getSpanEnd(oldSpan), s.getSpanFlags(oldSpan));
                s.removeSpan(oldSpan);
            }
            return s;
        } else {
            return text;
        }
    }

}
