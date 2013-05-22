package com.shopelia.android.widget.form;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.shopelia.android.config.Config;

public class FormLinearLayout extends LinearLayout implements FormContainer {

    public static final String LOG_TAG = "FormLinearLayout";

    private List<FormField> mFields = new ArrayList<FormField>();
    private ScrollView mParentScrollable;

    public FormLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        while (parent != null && parent != this && parent != parent.getParent()) {
            if (parent instanceof ScrollView) {
                mParentScrollable = (ScrollView) parent;
                break;
            }
            parent = parent.getParent();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        commit();
        for (FormField field : mFields) {
            field.onCreate(savedInstanceState);
        }
    }

    public void commit() {
        refreshFieldCache();
    }

    @Override
    public void updateSections() {
        boolean isSectionValid = true;
        for (int index = mFields.size() - 1; index >= 0; index--) {
            FormField field = mFields.get(index);
            if (field.isSectionHeader()) {
                field.setValid(isSectionValid);
                isSectionValid = true;
                field.invalidate();
            } else {
                isSectionValid = isSectionValid && field.isValid();
            }
        }
    }

    @Override
    public void requestFocus(FormField field) {
        field.requestFocus();
    }

    @Override
    public boolean nextField(FormField fromField) {
        int index = indexOf(fromField) + 1;
        final int size = mFields.size();
        for (; index < size; index++) {
            FormField f = mFields.get(index);
            if (!f.isValid() && !f.isSectionHeader()) {
                f.onNextField();
                return true;
            }
        }
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
        boolean out = true;
        for (FormField field : mFields) {
            boolean before = out;
            out = out & field.validate();
            if (before != out) {
                requestFocus(field);
            }
        }
        return out;
    }

    @Override
    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        for (FormField field : mFields) {
            if (field.getJsonPath() != null) {
                put(result, field.getJsonPath(), field.getResult());
            }
        }
        return result;
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

    private void put(Object json, String path, Object result) {
        StringBuilder newPath = new StringBuilder();
        String nodeName = getFirstNodeName(path, newPath);

        if (nodeName != null) {
            Object node = ((JSONObject) json).opt(nodeName);
            if (node == null) {
                String nextNode = getFirstNodeName(newPath.toString(), null);
                if (nextNode != null && nextNode.equals(ARRAY_INDICATOR)) {
                    node = new JSONArray();
                } else {
                    node = new JSONObject();
                }
                tryInserting(json, nodeName, node);
            }
            put(node, newPath.toString(), result);
        } else {
            tryInserting(json, path, result);
        }
    }

    private void tryInserting(Object json, String path, Object result) {
        try {
            if (json instanceof JSONObject) {
                ((JSONObject) json).put(path, result);
            } else if (json instanceof JSONArray) {
                ((JSONArray) json).put(result);
            } else {
                throw new UnsupportedOperationException();
            }
        } catch (JSONException e) {
            if (Config.INFO_LOGS_ENABLED) {
                Log.w(LOG_TAG, "Json error while computing form result", e);
            }
        } catch (UnsupportedOperationException e) {
            if (Config.INFO_LOGS_ENABLED) {
                Log.w(LOG_TAG, "Wrong path lead to invalid JSON node type", e);
            }
        }
    }

    private String getFirstNodeName(String path, StringBuilder newPath) {
        String nodeName = null;
        int sep = path.indexOf(PATH_SEPARATOR);
        if (sep != -1) {
            nodeName = path.substring(0, sep);
            if (newPath != null) {
                newPath.append(path.substring(sep + 1, path.length()));
            }
        }
        return nodeName;
    }

}