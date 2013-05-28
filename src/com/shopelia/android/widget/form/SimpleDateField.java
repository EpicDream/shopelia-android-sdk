package com.shopelia.android.widget.form;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

import com.shopelia.android.pretty.DateFormattingTextWatcher;

public class SimpleDateField extends NumberField {

    private DateFormattingTextWatcher mDateFormattingTextWatcher = new DateFormattingTextWatcher();

    public SimpleDateField(Context context) {
        this(context, null);
    }

    public SimpleDateField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleDateField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void setViewStyle(ViewHolder holder) {
        super.setViewStyle(holder);
        holder.editText.removeTextChangedListener(mDateFormattingTextWatcher);
        holder.editText.addTextChangedListener(mDateFormattingTextWatcher);
        holder.editText.setInputType(InputType.TYPE_CLASS_DATETIME);
    }

}
