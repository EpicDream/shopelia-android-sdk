package com.shopelia.android.adapter.form;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.R;
import com.shopelia.android.adapter.FormAdapter.Field;

public class HeaderField extends Field {

    public static final int TYPE = 0;

    private String mTitle;

    public HeaderField(String title) {
        super(TYPE);
        mTitle = title;
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
        out.setTag(holder);
        return out;
    }

    @Override
    public void bindView(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.title.setText(mTitle);
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
    public boolean isValid() {
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    private static class ViewHolder {
        TextView title;
    }

    @Override
    public boolean isSectionHeader() {
        return true;
    }

    @Override
    public boolean validate() {
        return isValid();
    }

}
