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
            int[] attrsArray = new int[] {
                    android.R.attr.text, // 0
                    android.R.attr.icon, // 1
            };
            TypedArray ta = getContext().obtainStyledAttributes(attrs, attrsArray);
            setText(ta.getText(0));
            ta.recycle();
        }
    }

    public void setText(CharSequence text) {
        mLabel.setText(text);
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
