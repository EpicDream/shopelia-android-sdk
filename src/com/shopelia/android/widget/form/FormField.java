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
import com.shopelia.android.widget.Errorable;

public abstract class FormField extends FrameLayout implements Errorable {

    public interface Listener {
        public void onValidChanged(FormField field);
    }

    public static class ListenerAdapter implements Listener {

        @Override
        public void onValidChanged(FormField field) {

        }

    }

    private static LayoutInflater sLayoutInflater;
    private static final String SAVE_ERRORS = "ERRORS_BUNDLE";

    private boolean mIsValid = false;

    private View mBoundedView;
    private FormContainer mFormContainer;
    private Listener mListener = new ListenerAdapter();
    private OnClickListener mOnClickListener;
    
    // Error management
    private boolean mError = false;
    private String mErrorMessage;
    private Bundle mErrorStack = new Bundle();

    public FormField(Context context) {
        this(context, null);
    }

    public FormField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public FormField setListener(Listener l) {
        mListener = l == null ? new ListenerAdapter() : l;
        return this;
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
    }

    protected void onAttachedToContainer(FormContainer container) {
        super.onAttachedToWindow();
        bindView(mBoundedView);
        mFormContainer = container;
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
     * Returns data held by this field as a {@link String}.
     * 
     * @return The result as String or null
     */
    public abstract String getResultAsString();

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
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle(getJsonPath() + SAVE_ERRORS, mErrorStack);
    }

    /**
     * Called each time the {@link FormAdapter} is being commited.
     * 
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(getJsonPath() + SAVE_ERRORS)) {
            mErrorStack = savedInstanceState.getBundle(getJsonPath() + SAVE_ERRORS);
        }
    }

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
        if (isValid && hasPreviousError()) {
            setError(mErrorStack.getString(getResultAsString()));
            isValid = false;
        }
        if (mIsValid != isValid) {
            mIsValid = isValid;
            if (getFormContainer() != null) {
                getFormContainer().updateSections();
            }
            if (isValid()) {
                setError(false);
            }
            mListener.onValidChanged(this);
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

    @Override
    public void setError(boolean hasError) {
        setError(hasError, null);
    }

    public void setError(String message) {
        setError(true, message);
    }

    protected void setError(boolean hasError, String message) {
        mErrorMessage = message;
        if (hasError != mError) {
            mError = hasError;
            if (hasError) {
                setValid(false);
                if (getResultAsString() != null) {
                    mErrorStack.putString(getResultAsString(), message);
                }
            }
            if (mBoundedView != null) {
                bindView(mBoundedView);
            }
        }
    }

    protected boolean hasPreviousError() {
        String result = getResultAsString();
        return result != null && mErrorStack.containsKey(result);
    }

    @Override
    public boolean hasError() {
        return mError;
    }

    public String getErrorMessage() {
        return hasError() ? mErrorMessage : null;
    }

    public void clearErrorsCache() {
        mErrorStack = new Bundle();
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

    @Override
    public void setOnClickListener(OnClickListener l) {
    	mOnClickListener = l;
    }
    
    public OnClickListener getOnClickListener() {
    	return mOnClickListener;
    }
    
    public boolean hasOnClickListener() {
    	return mOnClickListener != null;
    }
    
}
