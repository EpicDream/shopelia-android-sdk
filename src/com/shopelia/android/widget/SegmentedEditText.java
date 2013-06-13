package com.shopelia.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.shopelia.android.R;
import com.shopelia.android.text.method.ObfuscationTransformationMethod;
import com.shopelia.android.utils.IterableSparseArray;
import com.shopelia.android.utils.ViewUtils;
import com.shopelia.android.view.animation.ResizeAnimation;
import com.shopelia.android.view.animation.ResizeAnimation.OnViewRectComputedListener;

/**
 * An {@link EditText} that holds multiple segment of {@link EditText}. Each
 * {@link Segment} has its own content and can be automatically validate. The
 * validation of a field will pass the focus to the next {@link Segment}
 * 
 * @author Pierre Pollastri
 */
public class SegmentedEditText extends LinearLayout implements Errorable, Checkable {

    public static abstract class OnValidateListener implements TextWatcher {

        private Segment mSegment;

        public abstract boolean onValidate(Segment segment, CharSequence text);

        public void setSegment(Segment s) {
            mSegment = s;
        }

        public Segment getSegment() {
            return mSegment;
        }

        private boolean validate(CharSequence text) {
            mSegment.setError(false);
            if (onValidate(mSegment, text)) {
                mSegment.setChecked(true);
                return true;
            } else {
                mSegment.setChecked(false);
                return false;
            }
        }

        private void validateAndNext(CharSequence text) {
            if (validate(text) && !mSegment.isLockedForValidation()) {
                mSegment.nextSegment(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mSegment.setError(false);
            if (before == 0 && count == 1) {
                validateAndNext(s);
            } else if (!mSegment.isLockedForValidation()) {
                mSegment.setChecked(validate(s));
            }
        }
    }

    public interface OnErrorListener {

        public void onError(Segment s, CharSequence message);

    }

    public class Segment implements Errorable, Checkable {

        /**
         * The segment has no length limit
         */
        public static final int NO_LENGHT_LIMIT = 0;
        /**
         * Use the length limit of the parent {@link SegmentedEditText}
         */
        public static final int DEFAULT_LENGHT_LIMIT = -1;

        /**
         * Do not provide a specific id, let the {@link SegmentedEditText}
         * create an id for you.
         */
        public static final int AUTO_ID = -1;

        private int mLengthLimit = DEFAULT_LENGHT_LIMIT;
        private int mId = AUTO_ID;

        private boolean mFillParent = false;
        private boolean mHasError = false;
        private boolean mIsChecked = false;
        private boolean mHasFocus = false;

        private OnValidateListener mListener;

        private FormEditText mEditText;

        private boolean mLockedValidation = false;

        private ObfuscationTransformationMethod mObfuscation = new ObfuscationTransformationMethod(0, '\0');

        protected Segment(int id) {
            FormEditText editText = new FormEditText(getContext());
            editText.setBackgroundColor(Color.TRANSPARENT);
            editText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            editText.setMaxLines(1);
            editText.setPadding(getResources().getDimensionPixelSize(R.dimen.shopelia_segment_padding_v), getResources()
                    .getDimensionPixelSize(R.dimen.shopelia_segment_padding_h),
                    getResources().getDimensionPixelSize(R.dimen.shopelia_segment_padding_v),
                    getResources().getDimensionPixelSize(R.dimen.shopelia_segment_padding_h));
            editText.setTextColor(getResources().getColorStateList(R.color.shopelia_form_text_color_errorable));
            editText.setInputType(InputType.TYPE_CLASS_DATETIME);
            editText.setOnFocusChangeListener(mFocusObserver);
            editText.setOnEditorActionListener(mEditorActionListener);
            editText.setOnKeyListener(mOnKeyListener);
            editText.setSaveEnabled(false);
            mEditText = editText;
        }

        public void setObfuscationMethod(ObfuscationTransformationMethod method) {
            mObfuscation = method;
            if (mObfuscation == null) {
                mObfuscation = new ObfuscationTransformationMethod(0, '\0');
            }
        }

        public void lockValidation() {
            mLockedValidation = true;
        }

        public void unlockValidation() {
            mLockedValidation = false;
        }

        public boolean isLockedForValidation() {
            return mLockedValidation;
        }

        public Segment nextSegment(boolean animated) {
            return SegmentedEditText.this.nextSegment(this, animated);
        }

        public Segment previousSegment(boolean animated) {
            return SegmentedEditText.this.previousSegment(this, animated);
        }

        /**
         * Sets the segment property fillParent.
         * 
         * @param fillParent If true, the segment will fill the
         *            {@link SegmentedEditText} when it gain focus
         */
        public void setFillParent(boolean fillParent) {
            mFillParent = fillParent;
        }

        public boolean fillParent() {
            return mFillParent;
        }

        public View createView(Segment next) {
            mEditText.setId(mId);
            if (next != null) {
                mEditText.setNextFocusDownId(next.getId());
            }
            return mEditText;
        }

        public void setError(CharSequence message) {
            setError(message != null);
            if (mErrorListener != null) {
                mErrorListener.onError(this, message);
            }
        }

        public void setError(int resId) {
            setError(getResources().getString(resId));
        }

        public boolean hasFocus() {
            return mHasFocus;
        }

        public Context getContext() {
            return SegmentedEditText.this.getContext();
        }

        public Resources getResources() {
            return SegmentedEditText.this.getResources();
        }

        public FormEditText getView() {
            return mEditText;
        }

        public int getLengthLimit() {
            return mLengthLimit;
        }

        public int getId() {
            return mId;
        }

        public boolean requestFocus() {
            return getView() != null && getView().requestFocus();
        }

        public int getWidth() {
            if (fillParent()) {
                return -1;
            }

            if (isInEditMode()) {
                return getView().getWidth();
            }

            CharSequence text = getView().getText().length() > getView().getHint().length() ? getView().getText() : getView().getHint();
            return getView().getWidth() <= 0 ? ViewUtils.getTextViewBounds(getView(), text).width() + 10 : getView().getWidth();
        }

        public int getMaxWidth() {
            if (!fillParent()) {
                return getWidth();
            }
            return ViewUtils.getTextViewBounds(getView(), mObfuscation.getTransformation(getView().getText(), getView())).width();
        }

        public void gainFocus(boolean animated) {
            mHasFocus = true;
            if (animated && fillParent()) {
                getView().setTransformationMethod(new ObfuscationTransformationMethod());
                final ResizeAnimation anim = new ResizeAnimation(getView(), fillParent() ? LayoutParams.MATCH_PARENT
                        : LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                anim.setDuration(500);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.computeSize(new OnViewRectComputedListener() {

                    @Override
                    public void onViewRectComputed(View victim, Rect from, Rect to) {
                        getView().startAnimation(anim);
                    }
                });
            } else {
                int dimen = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? LayoutParams.WRAP_CONTENT : getWidth();
                getView().getLayoutParams().width = fillParent() ? LayoutParams.MATCH_PARENT : dimen;
            }

            unlockValidation();
        }

        public Editable getText() {
            return getView().getText();
        }

        public void loseFocus(boolean animated) {
            mHasFocus = false;
            lockValidation();
            if (animated && fillParent()) {
                getView().setTransformationMethod(mObfuscation);
                final ResizeAnimation anim = new ResizeAnimation(getView(), fillParent() ? computeSegmentWidth(this)
                        : LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                anim.setDuration(500);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.computeSize(new OnViewRectComputedListener() {

                    @Override
                    public void onViewRectComputed(View victim, Rect from, Rect to) {
                        getView().startAnimation(anim);
                    }
                });
            } else {
                int dimen = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? LayoutParams.WRAP_CONTENT : getWidth();
                getView().getLayoutParams().width = fillParent() ? LayoutParams.WRAP_CONTENT : dimen;
            }
            getView().clearComposingText();
        }

        public void setOnValidateListener(OnValidateListener l) {
            mListener = l;
            mListener.setSegment(this);
            getView().addTextChangedListener(mListener);
        }

        @Override
        public boolean isChecked() {
            return mIsChecked;
        }

        @Override
        public void setChecked(boolean checked) {
            if (checked != mIsChecked) {
                if (checked) {
                    mHasError = false;
                }
                mIsChecked = checked;
                getView().setChecked(checked);
                updateState();
            }
        }

        @Override
        public void toggle() {
            setChecked(!mIsChecked);
        }

        @Override
        public void setError(boolean hasError) {
            if (mHasError != hasError) {
                if (hasError) {
                    mIsChecked = false;
                }
                mHasError = hasError;
                getView().setError(hasError);
                updateState();
            }
        }

        @Override
        public boolean hasError() {
            return mHasError;
        }

        private OnEditorActionListener mEditorActionListener = new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean isValid = isValid();
                if ((actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) && !isValid) {
                    getView().requestFocus();
                    setError(true);
                    playWakeUp();
                    return true;
                }
                if ((actionId == EditorInfo.IME_ACTION_NEXT) && isValid) {
                    nextSegment(true);
                    return true;
                }

                return false;
            }
        };

        public boolean isValid() {
            return mListener == null ? true : mListener.validate(getView().getText());
        }

        public void playWakeUp() {
            getView().startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shopelia_soft_wakeup));
        }

        private OnKeyListener mOnKeyListener = new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN
                        && TextUtils.isEmpty(((TextView) v).getText())) {
                    Segment s = previousSegment(true);
                    if (s != null) {
                        s.getView().setSelection(s.getView().getText().length());
                    }
                    return true;
                }

                return false;
            }
        };

    }

    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };

    private static final int[] ERROR_STATE_SET = {
        R.attr.state_error
    };

    private static final int[] FOCUSED_STATE_SET = {
        android.R.attr.state_focused
    };

    private IterableSparseArray<Segment> mSegments = new IterableSparseArray<SegmentedEditText.Segment>();

    private OnErrorListener mErrorListener;

    private boolean mChecked = false;
    private boolean mError = false;
    private boolean mFocused = false;

    private Segment mFocusedSegment = null;
    private int mSegmentWidth = 100;

    public SegmentedEditText(Context context) {
        this(context, null);
    }

    public SegmentedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        setOrientation(LinearLayout.HORIZONTAL);
        if (getBackground() == null) {
            setBackgroundResource(R.drawable.shopelia_field);
        }
        if (isInEditMode()) {
            addSegment(createSegment());
            commit();
        }
        if (attrs != null) {
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SegmentedEditText, 0, 0);
            try {
                setSegmentWidth(ta.getDimensionPixelSize(R.styleable.SegmentedEditText_shopelia_segment_width, 100));
            } finally {
                ta.recycle();
            }
        }
    }

    public void setSegmentWidth(int width) {
        mSegmentWidth = width;
        invalidate();
    }

    public int getSegmentWidth() {
        return mSegmentWidth;
    }

    public Segment createSegment() {
        return createSegment(Segment.AUTO_ID);
    }

    public Segment createSegment(int id) {
        return new Segment(id);
    }

    public void addSegment(Segment segment) {
        if (segment == null) {
            return;
        }
        if (segment.getId() == Segment.AUTO_ID) {
            segment.mId = computeAvailableId();
        }
        mSegments.put(segment.mId, segment);
    }

    public void removeSegment(Segment segment) {
        mSegments.remove(segment.getId());
    }

    public void removeSegment(int id) {
        mSegments.remove(id);
    }

    public void setOnErrorListener(OnErrorListener l) {
        mErrorListener = l;
    }

    @Override
    public boolean hasFocus() {
        return mFocused;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        } else if (hasError()) {
            mergeDrawableStates(drawableState, ERROR_STATE_SET);
        } else if (hasFocus()) {
            mergeDrawableStates(drawableState, FOCUSED_STATE_SET);
        }

        return drawableState;
    }

    public void commit() {
        removeAllViews();
        final int count = mSegments.size();
        for (int index = 0; index < count; index++) {
            Segment segment = mSegments.valueAt(index);
            if (segment != null) {
                View v = segment.createView(getNextFocusableSegment(mSegments, index));
                v.setOnFocusChangeListener(mFocusObserver);
                addView(v);
            }
        }
        invalidateSegments(false);
    }

    private static Segment getNextFocusableSegment(SparseArray<Segment> segments, int index) {
        final int count = segments.size();
        while (++index < count) {
            Segment segment = segments.valueAt(index);
            if (segment != null) {
                return segment;
            }
        }
        return null;
    }

    private int computeAvailableId() {
        int available = mSegments.size();
        final int size = mSegments.size();
        for (int index = 0; index < size; index++) {
            if (mSegments.valueAt(index).getId() == available) {
                available++;
                index = -1;
            }
        }
        return available;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (checked != mChecked) {
            if (checked) {
                mError = false;
            }
            mChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public void setError(boolean hasError) {
        if (hasError != mError) {
            if (hasError) {
                mChecked = false;
            }
            mError = hasError;
            refreshDrawableState();
        }
    }

    @Override
    public boolean hasError() {
        return mError;
    }

    public void updateState() {
        boolean hasError = false;
        boolean isChecked = true;

        for (Segment s : mSegments) {
            if (s != null) {
                isChecked = isChecked && s.isChecked();
                hasError = hasError || s.hasError();
            }
        }

        setChecked(isChecked);
        setError(hasError);
    }

    public Segment nextSegment(Segment segment, boolean animated) {
        final int count = mSegments.size();
        for (int index = 0; index < count; index++) {
            Segment s = mSegments.valueAt(index);
            if (s == segment) {
                //@formatter:off
                while (++index < count && (s = mSegments.valueAt(index)) == null);
                //@formatter:on
                if (s != null) {
                    mFocusedSegment = s;
                    mFocusedSegment.requestFocus();
                    invalidateSegments(animated);
                    return s;
                }
            }
        }
        return null;
    }

    public Segment previousSegment(Segment segment, boolean animated) {
        final int count = mSegments.size();
        for (int index = 0; index < count; index++) {
            Segment s = mSegments.valueAt(index);
            if (s == segment) {
                //@formatter:off
                while (--index >= 0 && (s = mSegments.valueAt(index)) == null);
                //@formatter:on
                if (s != null) {
                    mFocusedSegment = s;
                    mFocusedSegment.requestFocus();
                    invalidateSegments(animated);
                    return s;
                }
            }
        }
        return null;
    }

    private OnFocusChangeListener mFocusObserver = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            mFocused = hasFocus;
            mFocusedSegment = null;
            boolean found = false;
            if (mSegments != null) {
                for (Segment s : mSegments) {
                    if (s != null && s.getView() == v && hasFocus) {
                        mFocused = true;
                        found = true;
                        if (mFocusedSegment != null) {
                            mFocusedSegment.loseFocus(true);
                        }
                        s.gainFocus(true);
                        mFocusedSegment = s;
                        break;
                    }
                }
            }
            refreshDrawableState();
            if (!found && mFocusedSegment == null) {
                invalidateSegments(true);
            }
        }
    };

    protected int computeSegmentWidth(Segment segment) {
        if (!segment.fillParent()) {
            return segment.getWidth();
        }
        int remainingWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int fillerSegmentCount = 0;
        for (Segment s : mSegments) {
            if (s != null) {
                if (s.fillParent()) {
                    fillerSegmentCount += 1;
                } else {
                    remainingWidth -= (s.getWidth());
                }
            }
        }
        return Math.min(remainingWidth / fillerSegmentCount, segment.getMaxWidth());
    }

    public void invalidateSegments(boolean animated) {
        boolean first = true;
        for (Segment s : mSegments) {
            if (s != null) {
                if (s.fillParent() && first && (mFocusedSegment == null && !s.isValid())) {
                    s.gainFocus(animated);
                } else if (s == mFocusedSegment) {
                    s.gainFocus(animated);
                } else {
                    s.loseFocus(animated);
                }
                first = false;
            }
        }
    }

}
