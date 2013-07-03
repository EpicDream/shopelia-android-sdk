package com.shopelia.android.widget.form;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;

public interface FormContainer {

    /**
     * The string used as a separator in JsonPath returned by
     * {@link Field#getJsonPath()}
     */
    public static final String PATH_SEPARATOR = ".";

    /**
     * The string used to represent an array entry. This should be put a t the
     * end of the path sequence.
     */
    public static final String ARRAY_INDICATOR = "#";

    public void updateSections();

    public void requestFocus(FormField field);

    public boolean nextField(FormField fromField);

    public void onActivityResult(int requestCode, int resultCode, Intent data);

    public void onCreate(Bundle savedInstanceState);

    public void onSaveInstanceState(Bundle outState);

    public int indexOf(FormField field);

    public boolean validate();

    public JSONObject toJson();

    public FormField findFieldByPath(String... path);

    public boolean removeField(int id);

    public boolean removeField(FormField field);

}
