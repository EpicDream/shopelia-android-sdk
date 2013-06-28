package com.shopelia.android.widget;

import android.widget.Checkable;

/**
 * Defines an extension for views that make them errorable. Actually it is the
 * same as {@link Checkable} for error with an ugly lastname
 * 
 * @author Pierre Pollastri
 */
public interface Errorable {

    /**
     * Change the checked state of the view
     */
    public void setError(boolean hasError);

    public boolean hasError();

}
