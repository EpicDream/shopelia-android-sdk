package com.shopelia.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.shopelia.android.R;
import com.shopelia.android.utils.IterableSparseArray;

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

        public abstract boolean onValidate(CharSequence text);

        @Override
        public void afterTextChanged(Editable s) {
            onValidate(s);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

    }

    public class Segment {

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

        private OnValidateListener mListener;

        private FormEditText mEditText;

        protected Segment(int id) {
            FormEditText editText = new FormEditText(getContext());
            editText.setBackgroundColor(Color.TRANSPARENT);
            editText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            editText.setMaxLines(1);
            editText.setPadding(getResources().getDimensionPixelSize(R.dimen.shopelia_segment_padding_v), getResources()
                    .getDimensionPixelSize(R.dimen.shopelia_segment_padding_h),
                    getResources().getDimensionPixelSize(R.dimen.shopelia_segment_padding_v),
                    getResources().getDimensionPixelSize(R.dimen.shopelia_segment_padding_h));
            editText.setInputType(InputType.TYPE_CLASS_DATETIME);
            editText.setOnFocusChangeListener(mFocusObserver);
            if (mListener != null && mListener.mSegment != this) {
                mListener.mSegment = this;
                editText.addTextChangedListener(mListener);
            }
            mEditText = editText;
        }

        public View createView(Segment next) {
            mEditText.setId(mId);
            if (next != null) {
                mEditText.setNextFocusDownId(next.getId());
            }
            return mEditText;
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

        public void setOnValidateListener(OnValidateListener l) {
            mListener = l;
            if (getView() != null) {
                mListener.mSegment = this;
                getView().addTextChangedListener(mListener);
            }
        }

    }

    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked, android.R.attr.state_enabled
    };

    private static final int[] ERROR_STATE_SET = {
            R.attr.state_error, android.R.attr.state_enabled
    };

    private static final int[] FOCUSED_STATE_SET = {
            android.R.attr.state_focused, android.R.attr.state_enabled
    };

    private IterableSparseArray<Segment> mSegments = new IterableSparseArray<SegmentedEditText.Segment>();

    private boolean mChecked = false;
    private boolean mError = false;
    private boolean mFocused = false;

    public SegmentedEditText(Context context) {
        this(context, null);
    }

    public SegmentedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (getBackground() == null) {
            setBackgroundResource(R.drawable.shopelia_field);
            getBackground().setVisible(false, false);
        }
        if (isInEditMode()) {
            addSegment(createSegment());
            commit();
        }
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

    @Override
    public boolean hasFocus() {
        return mFocused;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
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

    private OnFocusChangeListener mFocusObserver = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            mFocused = hasFocus;
            if (mSegments != null) {
                for (Segment s : mSegments) {
                    if (s != null && s.getView() == v && hasFocus) {
                        mFocused = true;
                    } else if (s != null && s.getView() == v && !hasFocus && s.mListener != null) {
                        s.mListener.onValidate(s.getView().getText());
                    }
                }
            }
            refreshDrawableState();
        }
    };

}
