package com.shopelia.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ExtendedFrameLayout extends FrameLayout {

    private boolean mBlockDraw = false;

    public ExtendedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
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

}
