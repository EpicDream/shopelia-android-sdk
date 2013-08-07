package com.shopelia.android.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.shopelia.android.R;
import com.shopelia.android.graphics.ColorDrawableAnimation;

@SuppressLint("DefaultLocale")
public class ValidationButton extends FrameLayout {

    private ImageView mIcon;
    private TextView mLabel;
    private View mSeparator;

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
            mSeparator = findViewById(R.id.separator);
        } catch (Exception e) {

        }
        if (attrs != null) {
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ValidationButton, 0, 0);
            try {
                setText(ta.getText(R.styleable.ValidationButton_shopelia_text));
                setIcon(ta.getDrawable(R.styleable.ValidationButton_shopelia_icon));
                setTextSize(ta.getDimension(R.styleable.ValidationButton_shopelia_textSize,
                        getResources().getDimension(R.dimen.shopelia_font_size_small)));
            } finally {
                ta.recycle();
            }
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
            text = text.toString().toUpperCase();
        }
        if (mLabel != null) {
            mLabel.setText(text);
        }
        refreshIcon();
    }

    public void setTextSize(float textSize) {
        mLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    public void setText(int resId) {
        setText(getContext().getString(resId));
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
            mSeparator.setVisibility(mIcon.getDrawable() != null ? View.VISIBLE : View.GONE);
        }
    }

    public void setIcon(int resId) {
        mIcon.setImageResource(resId);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled != isEnabled()) {
            super.setEnabled(enabled);
            final int count = getChildCount();
            for (int index = 0; index < count; index++) {
                getChildAt(index).setEnabled(enabled);
            }
            setCompatBackground(getResources().getDrawable(R.drawable.shopelia_white_button_background));
            if (!enabled) {
                {
                    ColorDrawableAnimation anim = new ColorDrawableAnimation(getBackground(), Color.WHITE);
                    anim.setDuration(getResources().getInteger(R.integer.shopelia_animation_time));
                    anim.start(getResources().getColor(R.color.shopelia_validation_green),
                            getResources().getColor(R.color.shopelia_validation_grey));
                }
                {
                    ColorDrawableAnimation anim = new ColorDrawableAnimation(mIcon.getDrawable(), Color.WHITE);
                    anim.setDuration(getResources().getInteger(R.integer.shopelia_animation_time));
                    anim.start(getResources().getColor(R.color.shopelia_validation_textColor_disabled));
                }
                {
                    ValueAnimator colorAnim = ObjectAnimator.ofInt(mLabel, "textColor", getResources().getColor(R.color.shopelia_white),
                            getResources().getColor(R.color.shopelia_validation_textColor_disabled));
                    colorAnim.setDuration(getResources().getInteger(R.integer.shopelia_animation_time));
                    colorAnim.setEvaluator(new ArgbEvaluator());
                    colorAnim.start();
                }
            } else {
                {
                    ColorDrawableAnimation anim = new ColorDrawableAnimation(getBackground(), Color.WHITE);
                    anim.setDuration(getResources().getInteger(R.integer.shopelia_animation_time));
                    anim.start(getResources().getColor(R.color.shopelia_validation_grey),
                            getResources().getColor(R.color.shopelia_validation_green));
                }
                {
                    ColorDrawableAnimation anim = new ColorDrawableAnimation(mIcon.getDrawable(), Color.WHITE);
                    anim.setDuration(getResources().getInteger(R.integer.shopelia_animation_time));
                    anim.start(getResources().getColor(R.color.shopelia_validation_textColor_disabled), Color.WHITE);
                }
                {
                    ValueAnimator colorAnim = ObjectAnimator.ofInt(mLabel, "textColor",
                            getResources().getColor(R.color.shopelia_validation_textColor_disabled),
                            getResources().getColor(R.color.shopelia_white));
                    colorAnim.setDuration(getResources().getInteger(R.integer.shopelia_animation_time));
                    colorAnim.setEvaluator(new ArgbEvaluator());
                    colorAnim.start();
                }
            }

        }
    }
}
