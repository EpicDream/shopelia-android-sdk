package com.shopelia.android.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.shopelia.android.R;

@SuppressLint("DefaultLocale")
public class ValidationButton extends FrameLayout {

    private ImageView mIcon;
    private TextView mLabel;

    public ValidationButton(Context context) {
        super(context);
        init(null);
    }

    public ValidationButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public ValidationButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    @SuppressLint("NewApi")
    private void init(AttributeSet attrs) {
        setClickable(true);
        LayoutInflater.from(getContext()).inflate(R.layout.shopelia_validation_button, this, true);
        try {
            mIcon = (ImageView) findViewById(R.id.icon);
            mLabel = (TextView) findViewById(R.id.text);
        } catch (Exception e) {

        }
        if (attrs != null) {
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ValidationButton, 0, 0);
            try {
                setText(ta.getText(R.styleable.ValidationButton_shopelia_text));
                setIcon(ta.getDrawable(R.styleable.ValidationButton_shopelia_icon));
                setTextSize(ta.getDimension(R.styleable.ValidationButton_shopelia_textSize, mLabel.getTextSize()));
            } finally {
                ta.recycle();
            }
        }
        if (super.getBackground() != null && super.getBackground() instanceof BitmapDrawable) {
            int paddingHorizontal = getChildAt(0).getPaddingTop();
            int paddingVertical = getChildAt(0).getPaddingLeft();
            setCompatBackground(super.getBackground());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                super.setBackground(null);
            } else {
                super.setBackgroundDrawable(null);
            }
            getChildAt(0).setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void setCompatBackground(Drawable background) {
        if (getChildCount() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                getChildAt(0).setBackground(background);
            } else {
                getChildAt(0).setBackgroundDrawable(background);
            }
        }
    }

    @Override
    public Drawable getBackground() {
        return getChildAt(0).getBackground();
    }

    public void setText(CharSequence text) {
        if (text != null) {
            // text = text.toString().toUpperCase();
        }
        if (mLabel != null) {
            mLabel.setText(text);
        }
        refreshIcon();
    }

    public void setTextSize(float textSize) {
        // mLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    public void setText(int resId) {
        try {
            setText(getContext().getString(resId));
        } catch (Exception e) {

        }
    }

    public CharSequence getText() {
        return mLabel.getText();
    }

    public void setIcon(Drawable drawable) {
        if (mIcon != null) {
            mIcon.setImageDrawable(drawable);
        }
        refreshIcon();
    }

    private void refreshIcon() {
        if (mIcon != null) {
            mIcon.setVisibility(mIcon.getDrawable() != null ? View.VISIBLE : View.GONE);
            // mSeparator.setVisibility(mIcon.getDrawable() != null ?
            // View.VISIBLE : View.GONE);
        }
    }

    public void setIcon(int resId) {
        mIcon.setImageResource(resId);
    }

    @Override
    public void setEnabled(boolean enabled) {

    }
}
