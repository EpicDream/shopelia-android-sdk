package com.shopelia.android.widget.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shopelia.android.R;

public class HeaderField extends FormField {

    public static final int TYPE = 0;

    private String mTitle;
    private Drawable mIcon;
    private boolean mHasIcon = false;
    private boolean mHasLock = false;
    private int[] mImagesIds = new int[0];

    public HeaderField(Context context) {
        this(context, null);
    }

    public HeaderField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.HeaderField, 0, 0);
            try {
                mTitle = ta.getString(R.styleable.HeaderField_shopelia_text);
                mIcon = ta.getDrawable(R.styleable.HeaderField_shopelia_icon);
            } finally {
                ta.recycle();
            }
        }
        mHasIcon = mIcon != null;
    }

    public HeaderField displayLock() {
        mHasLock = true;
        return this;
    }

    public HeaderField addPictures(int... resIds) {
        mImagesIds = resIds;
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

    }

    @Override
    public long getItemId() {
        return 0;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, ViewGroup viewGroup) {
        View out = inflater.inflate(R.layout.shopelia_form_field_title_header, viewGroup, false);
        ViewHolder holder = new ViewHolder();
        holder.title = (TextView) out.findViewById(R.id.title);
        holder.icon = (ImageView) out.findViewById(R.id.icon);
        holder.lock = (ImageView) out.findViewById(R.id.lock);
        holder.scroller = (LinearLayout) out.findViewById(R.id.scroller);
        out.setTag(holder);
        return out;
    }

    @Override
    public void bindView(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.title.setText(mTitle);
        holder.title.setTextColor(isValid() ? getContext().getResources().getColor(R.color.shopelia_headerTitleSectionOkColor)
                : getContext().getResources().getColor(R.color.shopelia_headerTitleSectionRegularColor));
        if (mHasIcon && isValid()) {
            holder.icon.setImageResource(R.drawable.shopelia_check_ok);
        } else if (mHasIcon && !isValid()) {
            holder.icon.setImageDrawable(mIcon);
        } else {
            holder.icon.setVisibility(View.GONE);
        }
        holder.lock.setVisibility(mHasLock ? View.VISIBLE : View.INVISIBLE);
        holder.scroller.removeAllViews();
        mImagesIds = new int[0];
        for (int resId : mImagesIds) {
            ImageView image = new ImageView(getContext());
            image.setImageResource(resId);
            image.setScaleType(ScaleType.CENTER_INSIDE);
            image.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            holder.scroller.addView(image);
        }
    }

    @Override
    public Object getResult() {
        return null;
    }

    @Override
    public String getJsonPath() {
        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    private static class ViewHolder {
        TextView title;
        ImageView icon;
        ImageView lock;
        LinearLayout scroller;
    }

    @Override
    public boolean isSectionHeader() {
        return true;
    }

    @Override
    public boolean validate() {
        return isValid();
    }

    @Override
    public void notifyDataChanged(View view) {
        super.notifyDataChanged(view);
        if (view != null) {
            bindView(view);
        }
    }

}
