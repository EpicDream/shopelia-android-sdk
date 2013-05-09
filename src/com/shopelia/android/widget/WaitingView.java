package com.shopelia.android.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * A special view drawing a loader with evolution displayed in percent. Note
 * that this view is only here to help the user waiting for Shopelia taking care
 * of the checkout process and the evolution of the progress is purely empiric.
 * 
 * @author Pierre Pollastri
 */
public class WaitingView extends View {

    private static final int MAX_ANGLE = 360;
    private static final long FRAMERATE = 40;
    private static final long FREQUENCY = 1000L / FRAMERATE;

    private static int STATE_STARTED = 0;
    private static int STATE_PAUSED = 1;
    private static int STATE_STOPPED = 2;

    private long mExpectedTime = 15000L;

    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private Handler mHandler = new Handler();

    // Variables
    private long mCurrentTime = 0L;
    private int mState = STATE_STOPPED;

    // Draw properties
    private float mProgressThickness = 40;
    private RectF mOval = new RectF();
    private Typeface mTypeface;

    // Render objects
    private Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mCenterCircleBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

    private int mTotalSteps;
    private int mCurrentStep;

    public WaitingView(Context context) {
        this(context, null);
    }

    public WaitingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaitingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();

        setProgressColor(0xFF2d9f35);
        setBackgroundColor(0xFFd2d2d2);
        setCenterCircleBackgroundColor(0xFFe5e5e5);
        setTextColor(0xFF737373);
    }

    public void init() {
        if (!isInEditMode()) {
            mTypeface = FontableTextView.tryCreateTypefaceFromAsset(getContext().getAssets(), FontableTextView.ASAP_BOLD);
        }

        if (mTypeface == null) {
            mTypeface = Typeface.DEFAULT_BOLD;
        }
        
        mCurrentStep = 1;
        mTotalSteps = 7;
        
        mCurrentTime = 500L;
    }

    public void restart() {
        stop();
        start();
    }

    public void start() {
        mInterpolator = new AccelerateDecelerateInterpolator();
        mHandler.removeCallbacks(mAnimationRunnable);
        mHandler.post(mAnimationRunnable);
    }

    public void pause() {
        mHandler.removeCallbacks(mAnimationRunnable);
    }

    public void stop() {
        mHandler.removeCallbacks(mAnimationRunnable);
        mCurrentTime = 0;
    }

    public void setProgressColor(int color) {
        mProgressPaint.setColor(color);
        mProgressPaint.setStyle(Style.FILL);
        invalidate();
    }

    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
        mBackgroundPaint.setStyle(Style.FILL);
        invalidate();
    }

    @SuppressLint("NewApi")
    public void setCenterCircleBackgroundColor(int color) {
        mCenterCircleBackgroundPaint.setColor(color);
        mCenterCircleBackgroundPaint.setStyle(Style.FILL);
        mCenterCircleBackgroundPaint.setShadowLayer(1.f, 1, 1.0f, 0x80000000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, mCenterCircleBackgroundPaint);
        }
        invalidate();
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        mTextPaint.setTypeface(mTypeface);
        mTextPaint.setTextAlign(Align.CENTER);
    }
    
    public void setTotalSteps(int steps) {
        mTotalSteps = steps;
    }
    
    public void newStep(String message) {
        mCurrentStep++;
        mCurrentTime = 0;
        
        mExpectedTime = 10000L;
        if (message != null) {
            Pattern p = Pattern.compile("_([0-9]+)$");
            Matcher m = p.matcher(message);
            if (m.find()) {
                try {
                    mExpectedTime = Integer.valueOf(m.group(1)) * 1000L;
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(parcelable);
        savedState.mCurrentTime = mCurrentTime;
        savedState.mExpectedTime = mExpectedTime;
        savedState.mState = mState;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mCurrentTime = savedState.mCurrentTime;
            mExpectedTime = savedState.mExpectedTime;
            mState = savedState.mState;
            if (mState == STATE_STARTED) {
                start();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float radius = getWidth() < getHeight() ? getWidth() / 2.f : getHeight() / 2.f;
        mProgressThickness = radius - radius * 0.80f;
        float x = getWidth() / 2.f;
        float y = getHeight() / 2.f;
        mOval.set(x - radius, y - radius, x + radius, y + radius);
        float progress = 0.f;
        if (!isInEditMode()) {
            float progressStep = (mCurrentTime < mExpectedTime ? mInterpolator.getInterpolation((float) mCurrentTime / (float) mExpectedTime) : 1.f);
            if (progressStep > 1.0f) {
                progressStep = 1.0f;
            }
            progress = (progressStep + (float) (mCurrentStep - 1f)) / (float) mTotalSteps;
        } else {
            progress = 0.45f;
        }
        if (progress > 1.0f) {
            progress = 1.0f;
        }

        int angle = (int) (MAX_ANGLE * progress);

        // Draw background "grey" circle
        canvas.drawCircle(x, y, radius, mBackgroundPaint);

        // Draw progress "green" indicator
        canvas.drawArc(mOval, -90, angle, true, mProgressPaint);

        // Draw center circle
        canvas.drawCircle(x, y, radius - mProgressThickness, mCenterCircleBackgroundPaint);

        String text = String.valueOf((int) (progress * 100));
        float textSize = radius * 0.40f;
        mTextPaint.setTextSize(textSize);
        float textX = x;
        float textY = y - (mTextPaint.ascent() + mTextPaint.descent()) / 2;
        text += "%";
        canvas.drawText(text, textX, textY, mTextPaint);

    }

    private Runnable mAnimationRunnable = new Runnable() {

        @Override
        public void run() {
            mCurrentTime += FREQUENCY;
            invalidate();
            mHandler.postDelayed(this, FREQUENCY);
            if (mCurrentTime > mExpectedTime * mTotalSteps + FREQUENCY) {
                stop();
            }
        }
    };

    private static class SavedState extends View.BaseSavedState {

        private long mCurrentTime;
        private long mExpectedTime;
        private int mState;
        private int mTotalSteps;
        private int mCurrentStep;

        public SavedState(Parcel source) {
            super(source);
            mCurrentTime = source.readLong();
            mExpectedTime = source.readLong();
            mState = source.readInt();
            mTotalSteps = source.readInt();
            mCurrentStep = source.readInt();
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(mCurrentTime);
            dest.writeLong(mExpectedTime);
            dest.writeInt(mState);
            dest.writeInt(mTotalSteps);
            dest.writeInt(mCurrentStep);
        }

    }

}
