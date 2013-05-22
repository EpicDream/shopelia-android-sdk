package com.shopelia.android.widget.form;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class FormLinearLayout extends LinearLayout implements FormContainer {

    private List<FormField> mFields = new ArrayList<FormField>();

    public FormLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        for (FormField field : mFields) {
            field.onCreate(savedInstanceState);
        }
    }

    @Override
    protected boolean addViewInLayout(View child, int index, android.view.ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        refreshFieldCache();
        return super.addViewInLayout(child, index, params, preventRequestLayout);
    }

    @Override
    public void updateSections() {

    }

    @Override
    public void requestFocus(FormField field) {

    }

    @Override
    public boolean nextField(FormField fromField) {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (FormField field : mFields) {
            field.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        for (FormField field : mFields) {
            field.onSaveInstanceState(outState);
        }
    }

    @Override
    public int indexOf(FormField field) {
        int index = 0;
        for (FormField f : mFields) {
            if (f == field) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public JSONObject toJson() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends FormField> T findFieldById(int id) {
        return (T) findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public <T extends FormField> T findFieldById(int id, Class<T> clazz) {
        return (T) findViewById(id);
    }

    private void refreshFieldCache() {
        final int count = getChildCount();
        mFields.clear();
        for (int position = 0; position < count; position++) {
            View child = getChildAt(position);
            if (child instanceof FormField) {
                mFields.add((FormField) child);
            }
        }
    }

}
