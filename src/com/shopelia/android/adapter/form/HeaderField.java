package com.shopelia.android.adapter.form;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.shopelia.android.R;
import com.shopelia.android.adapter.FormAdapter.Field;

public class HeaderField extends Field {

    public static final int TYPE = 0;

    private String mTitle;
    private int mIconId;
    private boolean mHasIcon = false;
    private boolean mHasLock = false;
    private int[] mImagesIds = new int[0];

    public HeaderField(String title) {
        super(TYPE);
        mTitle = title;
    }

    public HeaderField(Context context, int resId, int iconId) {
        this(context.getString(resId));
        mIconId = iconId;
        mHasIcon = true;
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
        if (mHasIcon == true) {
            holder.icon.setImageResource(isValid() ? R.drawable.shopelia_check_ok : mIconId);
        } else {
            holder.icon.setVisibility(View.GONE);
        }
        holder.lock.setVisibility(mHasLock ? View.VISIBLE : View.INVISIBLE);
        holder.scroller.removeAllViews();
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
