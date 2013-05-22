package com.shopelia.android.widget.form;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.shopelia.android.adapter.FormAdapter;
import com.shopelia.android.adapter.FormAdapter.Field;

public abstract class FormField extends FrameLayout {

    private static LayoutInflater sLayoutInflater;

    private boolean mIsValid = false;

    private View mBoundedView;
    private FormContainer mFormContainer;

    public FormField(Context context) {
        this(context, null);
    }

    public FormField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FormField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (sLayoutInflater == null || sLayoutInflater.getContext() != context) {
            sLayoutInflater = LayoutInflater.from(context);
        }
        mBoundedView = createView(getContext(), sLayoutInflater, this);
        removeAllViews();
        addView(mBoundedView);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        bindView(mBoundedView);
        mFormContainer = (FormContainer) getParent();
    }

    /**
     * Return the id of the item. 0 if it has no id.
     * 
     * @return
     */
    public abstract long getItemId();

    /**
     * Create a new view for the {@link Field}. You should not bind data in this
     * method because {@link Field#bindView(View)} will be called after. <br/>
     * <b>Note:</b> You should create your VieHolder pattern here.
     * 
     * @param context
     * @param inflater
     * @param viewGroup
     * @return
     */
    public abstract View createView(Context context, LayoutInflater inflater, ViewGroup viewGroup);

    public abstract void bindView(View view);

    /**
     * Returns data held by this field. It could either {@link String},
     * {@link Long}, {@link Integer}, {@link Boolean}, {@link Double},
     * {@link Float}, {@link JSONObject} or {@link JSONArray}.
     * 
     * @return
     */
    public abstract Object getResult();

    /**
     * The path in the final {@link JSONObject} to retrieve this field result.
     * Json keys are separated with '.' and '#' indicates an array.
     * 
     * @return
     */
    public abstract String getJsonPath();

    /**
     * Indicates if data are valid or not
     * 
     * @return
     */
    public boolean isValid() {
        return mIsValid;
    }

    /**
     * This method is called when it is time validate the field and fire error
     * in case of invalid data.
     * 
     * @return
     */
    public abstract boolean validate();

    /**
     * Called when memory will be released and field should save its data to
     * recover state later.
     * 
     * @param outState
     */
    public abstract void onSaveInstanceState(Bundle outState);

    /**
     * Called each time the {@link FormAdapter} is being commited.
     * 
     * @param savedInstanceState
     */
    public abstract void onCreate(Bundle savedInstanceState);

    /**
     * Indicates if the given field is a section header or not
     * 
     * @return
     */
    public abstract boolean isSectionHeader();

    /**
     * This method changes the validity of the field. This method will ask to
     * the form to compute its sections.
     * 
     * @param isValid
     */
    public void setValid(boolean isValid) {
        if (mIsValid != isValid) {
            mIsValid = isValid;
            if (getFormContainer() != null) {
                getFormContainer().updateSections();
            }
        }
    }

    /**
     * Returns the attached FormWidget. <br />
     * <b>Note:</b> This method could return a null value.
     * 
     * @return
     */
    public FormContainer getFormContainer() {
        return mFormContainer;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    /**
     * Returns the bounded view if it exists otherwise returns null
     * 
     * @return
     */
    public View getBoundedView() {
        return mBoundedView;
    }

    /**
     * Called when {@link FormAdapter#nextField(Field)} is called
     */
    public void onNextField() {

    }

    @Override
    public void invalidate() {
        super.invalidate();
        bindView(mBoundedView);
    }

}
