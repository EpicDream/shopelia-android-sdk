package com.shopelia.android.widget.form;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.shopelia.android.R;
import com.shopelia.android.pretty.CardNumberFormattingTextWatcher;
import com.shopelia.android.pretty.DateFormattingTextWatcher;
import com.shopelia.android.widget.SegmentedEditText;
import com.shopelia.android.widget.SegmentedEditText.Segment;

public class SingleLinePaymentCardField extends FormField {

    public SingleLinePaymentCardField(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public long getItemId() {
        return 0;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, ViewGroup viewGroup) {
        View v = inflater.inflate(R.layout.shopelia_form_field_single_line_payment, viewGroup, false);
        SegmentedEditText editText = (SegmentedEditText) v.findViewById(R.id.edit_text);
        Segment segment = editText.createSegment();
        segment.getView().setHint("1234 5678 9012 3456");
        segment.setFillParent(true);
        segment.setOnValidateListener(sPaymentCardValidator);
        segment.getView().addTextChangedListener(new CardNumberFormattingTextWatcher());
        segment.getView().setFilters(new InputFilter[] {
            new InputFilter.LengthFilter(19)
        });
        editText.addSegment(segment);

        segment = editText.createSegment();
        segment.getView().setHint("MM/YY");
        segment.getView().addTextChangedListener(new DateFormattingTextWatcher());
        segment.getView().setFilters(new InputFilter[] {
            new InputFilter.LengthFilter(5)
        });
        segment.setOnValidateListener(sExpiryDateValidator);
        editText.addSegment(segment);

        segment = editText.createSegment();
        segment.getView().setHint("CVV");
        segment.getView().setFilters(new InputFilter[] {
            new InputFilter.LengthFilter(3)
        });
        segment.setOnValidateListener(sCvvValidator);
        editText.addSegment(segment);

        editText.commit();

        return v;
    }

    @Override
    public void bindView(View view) {

    }

    @Override
    public Object getResult() {
        return null;
    }

    @Override
    public String getResultAsString() {
        return null;
    }

    @Override
    public String getJsonPath() {
        return null;
    }

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }

    private static final SegmentedEditText.OnValidateListener sPaymentCardValidator = new SegmentedEditText.OnValidateListener() {

        @Override
        public boolean onValidate(Segment segment, CharSequence text) {
            String number = text.toString().replace(" ", "");
            if (number.length() == 16) {
                segment.nextSegment(true);
                return true;
            }
            return false;
        }
    };

    private static final SegmentedEditText.OnValidateListener sExpiryDateValidator = new SegmentedEditText.OnValidateListener() {

        @Override
        public boolean onValidate(Segment segment, CharSequence text) {
            if (text.length() == 5) {
                segment.nextSegment(true);
                return true;
            }
            return false;
        }
    };

    private static final SegmentedEditText.OnValidateListener sCvvValidator = new SegmentedEditText.OnValidateListener() {

        @Override
        public boolean onValidate(Segment segment, CharSequence text) {
            if (text.length() == 3) {
                InputMethodManager imm = (InputMethodManager) segment.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(segment.getView().getWindowToken(), 0);
                return true;
            }
            return false;
        }
    };

}
