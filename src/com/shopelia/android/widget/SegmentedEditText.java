package com.shopelia.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
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
public class SegmentedEditText extends LinearLayout {

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

    }

    private IterableSparseArray<Segment> mSegments = new IterableSparseArray<SegmentedEditText.Segment>();

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
        for (Segment s : mSegments) {
            if (s != null && s.getView() != null && s.getView().hasFocus()) {
                return true;
            }
        }
        return false;
    }

    public void commit() {
        removeAllViews();
        final int count = mSegments.size();
        for (int index = 0; index < count; index++) {
            Segment segment = mSegments.valueAt(index);
            if (segment != null) {
                addView(segment.createView(getNextFocusableSegment(mSegments, index)));
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

}
