package com.shopelia.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.os.SystemClock;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.shopelia.android.utils.TimeUnits;

public class ProgressTextView extends FontableTextView implements Animatable {

    private static long FRAME_DURATION = TimeUnits.SECONDS / 30;

    private Rect mTextBounds = new Rect();
    private Rect mTextAloneBounds = new Rect();
    private boolean mIsRunning = false;
    private AnimatedChar[] mChars;
    private Interpolator mInInterpolator = new AccelerateDecelerateInterpolator();
    private Interpolator mOutInterpolator = new AccelerateDecelerateInterpolator();
    private long mInAnimationDuration = TimeUnits.SECONDS;

    public ProgressTextView(Context context) {
        this(context, null);
    }

    public ProgressTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final int width = right - left;
        final int height = bottom - top;
        mChars = AnimatedChar.create(3, '.', getPaint());
        String text = getText() + AnimatedChar.stringify(mChars);
        getPaint().getTextBounds(text, 0, text.length(), mTextBounds);
        getPaint().getTextBounds(getText().toString(), 0, getText().length(), mTextAloneBounds);
        mTextBounds.offsetTo((int) (width / 2.f - mTextBounds.width() / 2.f), (int) (height / 2.f - mTextBounds.height() / 2.f));
        mTextAloneBounds.offsetTo(mTextBounds.left, mTextBounds.top);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        canvas.drawText(getText().toString(), mTextBounds.left, mTextBounds.top + getBaseline(), paint);
        final int alpha = paint.getAlpha();
        final float size = paint.getTextSize();
        for (int index = 0; index < mChars.length; index++) {
            AnimatedChar c = mChars[index];
            if (c.alpha > 0) {
                getPaint().setAlpha(c.alpha);
                canvas.drawText(c.toString(), mTextAloneBounds.right + (index + 1) * (c.bounds.left + c.bounds.width()) + c.x,
                        (mTextAloneBounds.top + getBaseline()) + c.y, paint);
            }
        }
        paint.setAlpha(alpha);
        paint.setTextSize(size);
    }

    private Runnable mUpdater = new Runnable() {

        @Override
        public void run() {
            long now = AnimationUtils.currentAnimationTimeMillis();
            Log.d(null, "UPDATE");
            for (int index = 0; index < mChars.length; index++) {
                AnimatedChar c = mChars[index];
                if (c.anim == null) {
                    c.anim = new InAnim(c, mInInterpolator, mInAnimationDuration, (int) (mTextBounds.right * 1.5F));
                    c.anim.start();
                }
                c.anim.apply(now);
            }
            if (isRunning()) {
                postDelayed(mUpdater, SystemClock.uptimeMillis() + FRAME_DURATION);
            }
            invalidate();
        }
    };

    @Override
    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void start() {
        if (!mIsRunning) {
            Log.d(null, "START");
            mIsRunning = true;
            post(mUpdater);
        }
    }

    @Override
    public void stop() {
        if (mIsRunning) {
            mIsRunning = false;
        }
    }

    private static class AnimatedChar {
        private String c;
        private Rect bounds = new Rect();
        private int alpha = 255;
        private int x = 0;
        private int y = 0;
        private Anim anim;
        private long delay = 200 * TimeUnits.MILISECONDS;

        public AnimatedChar(char c) {
            this.c = String.valueOf(c);
        }

        public void computeBounds(TextPaint paint) {
            paint.getTextBounds(toString(), 0, 1, bounds);
        }

        public static AnimatedChar[] create(int number, char c, TextPaint paint) {
            AnimatedChar[] out = new AnimatedChar[number];

            for (int index = 0; index < number; index++) {
                out[index] = new AnimatedChar(c);
                if (index == 0) {
                    out[index].computeBounds(paint);
                } else {
                    out[index].bounds = out[0].bounds;
                }
            }
            return out;
        }

        @Override
        public String toString() {
            return c;
        }

        public static String stringify(AnimatedChar[] chars) {
            StringBuilder builder = new StringBuilder();
            for (AnimatedChar c : chars) {
                builder.append(c.toString());
            }
            return builder.toString();
        }

    }

    private static abstract class Anim {

        private Interpolator interpolator;
        private long duration = TimeUnits.SECONDS;
        private long startTime;
        public final AnimatedChar target;

        public Anim(AnimatedChar target, Interpolator interpolator, long duration) {
            this.interpolator = interpolator;
            this.duration = duration;
            this.target = target;
        }

        public void start() {
            startTime = AnimationUtils.currentAnimationTimeMillis();
        }

        public void apply(long now) {
            apply(interpolator.getInterpolation((float) (now - startTime) / (float) duration));
        }

        public abstract void finish();

        public abstract void apply(float interpolatedTime);

        public boolean isFinished(long now) {
            return (now - startTime) >= duration;
        }
    }

    private static class InAnim extends Anim {

        public int x;

        public InAnim(AnimatedChar target, Interpolator interpolator, long duration, int beginX) {
            super(target, interpolator, duration);
            this.x = beginX;
        }

        @Override
        public void finish() {

        }

        @Override
        public void apply(float interpolatedTime) {
            target.x = (int) (x * (1.f - interpolatedTime));
            target.alpha = (int) (255 * (interpolatedTime));
        }

    }

}
