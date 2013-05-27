package com.shopelia.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.shopelia.android.R;

public class NumberInput extends EditText implements Errorable {

    private int mMaxLength = 4;
    private Drawable mDrawable;
    private Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean mHasError = false;

    public NumberInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.NumberInput, 0, 0);
            try {
                mDrawable = ta.getDrawable(R.styleable.NumberInput_shopelia_number_background);
            } finally {
                ta.recycle();
            }
        }
        mCirclePaint.setStyle(Style.FILL);
        mCirclePaint.setColor(Color.BLACK);
        setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int maxLength = mMaxLength;

        if (isInEditMode()) {
            maxLength = 4;
        }

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int width = getWidth() - (paddingLeft + paddingRight);
        int height = getHeight() - (paddingTop + paddingBottom);

        int inputWidth = width / mMaxLength;
        int inputHeight = inputWidth < height ? inputWidth : height;

        inputWidth = inputHeight;

        int space = (width - inputWidth * mMaxLength) / mMaxLength;

        paddingLeft += space / 2;

        paddingTop += (height / 2 - inputHeight / 2);

        int textLength = getText().length();

        if (isInEditMode()) {
            textLength = 2;
        }

        float radius = inputWidth / 8.f;
        for (int index = 0; index < maxLength; index++) {
            int left = index * (inputWidth + space) + paddingLeft;
            int top = paddingTop;
            Drawable drawable = getDrawable(index, textLength);
            drawable.setBounds(left, top, left + inputWidth, top + inputHeight);
            drawable.draw(canvas);
            if (index < textLength) {
                float cx = left + inputWidth / 2.f;
                float cy = top + inputHeight / 2.f;
                canvas.drawCircle(cx, cy, radius, mCirclePaint);
            }
        }
    }

    protected Drawable getDrawable(final int index, final int length) {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mDrawable;
            if (!isEnabled()) {
                drawable.setState(new int[] {});
            } else if (hasError()) {
                drawable.setState(new int[] {
                    R.attr.state_error
                });
            } else if (index == length && hasFocus()) {
                drawable.setState(new int[] {
                    android.R.attr.state_focused
                });
            } else if (index < length) {
                drawable.setState(new int[] {
                    android.R.attr.state_checked
                });
            } else {
                drawable.setState(new int[] {});
            }
            return drawable;
        } else {
            return mDrawable;
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        setSelection(getText().length(), getText().length());
    }

    @Override
    public void setError(boolean hasError) {
        mHasError = hasError;
        invalidate();
    }

    @Override
    public boolean hasError() {
        return mHasError;
    }

}
