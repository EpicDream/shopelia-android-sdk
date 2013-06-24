package com.shopelia.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.shopelia.android.R;

/**
 * An extended {@link FrameLayout} able to block the drawing of the layout,
 * disable recursive drawable state dispatch, ...
 * 
 * @author Pierre Pollastri
 */
public class ExtendedFrameLayout extends FrameLayout {

    private boolean mBlockDraw = false;
    private boolean mEnableRecursivePressed;

    public ExtendedFrameLayout(Context context) {
        super(context);
        init(context, null);
    }

    public ExtendedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ExtendedFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!mBlockDraw) {
            super.dispatchDraw(canvas);
        }
    }

    public void lockDraw() {
        mBlockDraw = true;
        invalidate();
    }

    public void unlockDraw() {
        mBlockDraw = false;
        invalidate();
    }

    protected void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtendedFrameLayout);
            try {
                mEnableRecursivePressed = a.getBoolean(R.styleable.ExtendedFrameLayout_shopelia_enable_dispatch_drawable_state, false);
            } finally {
                if (a != null) {
                    a.recycle();
                }
            }
        }
    }

    @Override
    public void setPressed(boolean pressed) {
        View parent = (View) getParent();
        if ((parent != null && parent.isPressed() && mEnableRecursivePressed) || (parent != null && !parent.isPressed())) {
            super.setPressed(pressed);
        }
    }

}
