package com.shopelia.android.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.shopelia.android.R;

public class TicketDrawable extends Drawable {

    private static int COLOR_BORDER = 0xFFD3D3D3;
    private static int COLOR_BACKGROUND = Color.WHITE;

    private int mAlpha = 255;
    private int mTriangleCount = 16;
    private int mTriangleHeight = 20;

    private RectF mMainRectBounds = new RectF();
    private Path mFooterPath = new Path();
    private int mRectCornerRadius = 5;

    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public TicketDrawable(Context context) {
        mBackgroundPaint.setStyle(Style.FILL);
        mBackgroundPaint.setColor(COLOR_BACKGROUND);

        mBorderPaint.setStyle(Style.STROKE);
        mBorderPaint.setStrokeWidth(1);
        mBorderPaint.setColor(COLOR_BORDER);

        mTriangleHeight = context.getResources().getDimensionPixelSize(R.dimen.shopelia_ticket_triangle_height);
        mRectCornerRadius = context.getResources().getDimensionPixelSize(R.dimen.shopelia_ticket_border_radius);
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas.getWidth() != mMainRectBounds.width() || canvas.getHeight() != mMainRectBounds.height()) {
            mMainRectBounds.set(0, 0, canvas.getWidth(), canvas.getHeight());
            computeFooterPath(canvas);
        }
        canvas.save();

        canvas.clipRect(0, 0, canvas.getWidth(), canvas.getHeight() - mTriangleHeight);
        canvas.drawRoundRect(mMainRectBounds, mRectCornerRadius, mRectCornerRadius, mBackgroundPaint);
        canvas.drawRoundRect(mMainRectBounds, mRectCornerRadius, mRectCornerRadius, mBorderPaint);
        canvas.restore();
        canvas.drawPath(mFooterPath, mBackgroundPaint);
        canvas.drawPath(mFooterPath, mBorderPaint);
    }

    @Override
    public int getOpacity() {
        return mAlpha;
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
    }

    @Override
    public void setColorFilter(ColorFilter filter) {

    }

    private void computeFooterPath(Canvas canvas) {
        final float left = 0;
        final float top = canvas.getHeight() - mTriangleHeight;
        final float right = canvas.getWidth();
        final float bottom = canvas.getHeight();

        final float triangleBaseWidth = canvas.getWidth() / mTriangleCount;

        mFooterPath.reset();
        mFooterPath.setFillType(FillType.EVEN_ODD);

        float relativeLeft = left + triangleBaseWidth / 2.f;

        mFooterPath.moveTo(left, top);
        mFooterPath.lineTo(left, bottom);
        mFooterPath.lineTo(relativeLeft, top);

        float relativeRight = relativeLeft;
        float relativeCenter = 0;
        for (int index = 1; index < mTriangleCount; index++) {
            relativeRight = relativeLeft + triangleBaseWidth;
            relativeCenter = relativeLeft + (triangleBaseWidth) / 2.f;
            mFooterPath.lineTo(relativeCenter, bottom);
            mFooterPath.lineTo(relativeRight, top);
            relativeLeft = relativeRight;
        }

        mFooterPath.lineTo(right, bottom);
        mFooterPath.lineTo(right, top);
        // mFooterPath.lineTo(left, top);
        // mFooterPath.close();
    }
}
