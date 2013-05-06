package com.shopelia.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.shopelia.android.R;

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

    private void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.shopelia_validation_button, this, true);
        mIcon = (ImageView) findViewById(R.id.icon);
        mLabel = (TextView) findViewById(R.id.text);
        if (attrs != null) {
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ValidationButton, 0, 0);
            try {
                setText(ta.getText(R.styleable.ValidationButton_text));
                setIcon(ta.getDrawable(R.styleable.ValidationButton_icon));
            } finally {
                ta.recycle();
            }
        }
    }

    public void setText(CharSequence text) {
        if (text != null) {
            text = text.toString().toUpperCase();
        }
        if (mLabel != null) {
            mLabel.setText(text);
        }
    }

    public void setText(int resId) {
        mLabel.setText(resId);
    }

    public void setIcon(Drawable drawable) {
        mIcon.setImageDrawable(drawable);
    }

    public void setIcon(int resId) {
        mIcon.setImageResource(resId);
    }

}
