package com.shopelia.android.adapter;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FormAdapter extends BaseAdapter {

    public enum FieldType {
        Header, EditText,

    }

    public abstract static class Field {

        private final FieldType mFieldType;

        protected Field(FieldType type) {
            mFieldType = type;
        }

        public FieldType getFieldType() {
            return mFieldType;
        }

    }

    private List<Field> mFieldList;

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        return null;
    }

}
