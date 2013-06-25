package com.shopelia.android.widget.form;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.shopelia.android.R;
import com.shopelia.android.config.Config;
import com.shopelia.android.utils.JsonUtils;

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
        Log.d(null, "NEXT FIELD");
        int index = indexOf(fromField) + 1;
        final int size = mFields.size();
        for (; index < size; index++) {
            FormField f = mFields.get(index);
            if ((!f.isValid() || TextUtils.isEmpty(f.getResultAsString())) && !f.isSectionHeader()) {
                Log.d(null, "NEXT FIELD CONFIRMED " + f.getId() + " " + R.id.name);
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
        mFields.clear();
        refreshFieldCache(this);
    }

    private void refreshFieldCache(ViewGroup viewGroup) {
        final int count = viewGroup.getChildCount();
        for (int position = 0; position < count; position++) {
            View child = viewGroup.getChildAt(position);
            if (child instanceof FormField) {
                mFields.add((FormField) child);
                ((FormField) child).onAttachedToContainer(this);
            } else if (child instanceof ViewGroup) {
                refreshFieldCache((ViewGroup) child);
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
                if (result instanceof JSONObject) {
                    JsonUtils.mergeObject((JSONObject) json, path, (JSONObject) result);
                } else {
                    ((JSONObject) json).put(path, result);
                }
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

    @Override
    public FormField findFieldByPath(String... path) {
        if (path.length == 0) {
            return null;
        }
        StringBuilder p = new StringBuilder();
        for (String item : path) {
            p.append(item);
            p.append(FormContainer.PATH_SEPARATOR);
        }
        if (p.length() > 0) {
            p.deleteCharAt(p.length() - 1);
        }
        String pathString = p.toString();
        for (FormField field : mFields) {
            if (pathString.equals(field.getJsonPath())) {
                return field;
            }
        }
        return null;
    }

    @Override
    public boolean removeField(int id) {
        return removeField(findFieldById(id));
    }

    @Override
    public boolean removeField(FormField field) {
        int index;
        refreshFieldCache();
        if (field != null && (index = indexOf(field)) != -1) {
            ((ViewGroup) field.getParent()).removeView(field);
            mFields.remove(index);
            return true;
        }
        return false;
    }
}
