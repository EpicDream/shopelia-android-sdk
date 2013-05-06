package com.shopelia.android.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.shopelia.android.config.Config;

public class FormAdapter extends BaseAdapter {

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

    private static final String LOG_TAG = "FormAdapter";

    /**
     * Form fields handle view creation and binding. Note that type id must be
     * consistent with there view composition in order to be nicely handle by
     * the Adapter.
     * 
     * @author Pierre Pollastri
     */
    public abstract static class Field {

        private final int mType;
        private boolean mIsValid = false;
        private FormAdapter mAdapter;
        private String mJSonPath;

        protected Field(int type) {
            mType = type;
        }

        private void setAdapter(FormAdapter adapter) {
            mAdapter = adapter;
        }

        public int getFieldType() {
            return mType;
        }

        /**
         * Return the id of the item. 0 if it has no id.
         * 
         * @return
         */
        public abstract long getItemId();

        /**
         * Create a new view for the {@link Field}. You should not bind data in
         * this method because {@link Field#bindView(View)} will be called
         * after. <br/>
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
         * The path in the final {@link JSONObject} to retrieve this field
         * result. Json keys are separated with '.' and '#' indicates an array.
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
         * This method is called when it is time validate the field and fire
         * error in case of invalid data.
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
         * This method changes the validity of the field. This method will ask
         * to the form to compute its sections.
         * 
         * @param isValid
         */
        public void setValid(boolean isValid) {
            if (mIsValid != isValid) {
                mIsValid = isValid;
                if (getAdapter() != null) {
                    getAdapter().updateSections();
                }
            }
        }

        /**
         * Returns the attached adapter. <br />
         * <b>Note:</b> This method could return a null value.
         * 
         * @return
         */
        public FormAdapter getAdapter() {
            return mAdapter;
        }

        /**
         * Called each time that data changed and adapter do not want to notify
         * its {@link AdapterView}
         */
        public void notifyDataChanged(View view) {

        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {

        }

        public Context getContext() {
            return getAdapter().getContext();
        }

        /**
         * Returns the bounded view if it exists otherwise returns null
         * 
         * @return
         */
        public View getBoundedView() {
            if (mAdapter == null) {
                return null;
            }
            int index = mAdapter.mFieldList.indexOf(this);
            return mAdapter.getViewByIndex(index);
        }

    }

    private List<Field> mFieldList = new ArrayList<FormAdapter.Field>();
    private HashSet<Integer> mFieldTypes = null;
    private LayoutInflater mInflater;
    private Context mContext;
    private AdapterView<?> mAdapterView;

    public FormAdapter(AdapterView<?> adapterView) {
        mContext = adapterView.getContext();
        mAdapterView = adapterView;
        mInflater = LayoutInflater.from(mContext);
    }

    /**
     * A a single field to the {@link FormAdapter}
     * 
     * @param field
     * @return The form adapter in order to be chained.
     */
    public FormAdapter add(Field field) {
        mFieldList.add(field);
        return this;
    }

    /**
     * Add a list of fields to the {@link FormAdapter}
     * 
     * @param fields
     * @return The form adapter in order to be chained.
     */
    public FormAdapter add(List<Field> fields) {
        for (Field field : fields) {
            add(field);
        }
        return this;
    }

    public void commit(Bundle savedInstanceState) {
        mFieldTypes = new HashSet<Integer>();
        for (Field field : mFieldList) {
            mFieldTypes.add(field.getFieldType());
            field.setAdapter(this);
            field.onCreate(savedInstanceState);
        }
        updateSections();
        notifyDataSetChanged();
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public int getCount() {
        return mFieldList.size();
    }

    @Override
    public Object getItem(int location) {
        return mFieldList.get(location);
    }

    @Override
    public long getItemId(int location) {
        return mFieldList.get(location).getItemId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        Field field = (Field) getItem(position);
        if (convertView == null) {
            convertView = field.createView(mContext, mInflater, viewGroup);
        }
        field.bindView(convertView);
        return convertView;
    }

    public Field getField(int index) {
        return mFieldList.get(index);
    }

    public Field getField(String jsonPath, Class<? extends Field> clazz) {
        if (jsonPath == null || clazz == null) {
            return getField(jsonPath);
        }
        for (Field field : mFieldList) {
            if (jsonPath.equals(field.getJsonPath()) && clazz.isInstance(field)) {
                return field;
            }
        }
        return null;
    }

    public Field getField(String jsonPath) {
        if (jsonPath == null) {
            return null;
        }
        for (Field field : mFieldList) {
            if (TextUtils.equals(field.getJsonPath(), jsonPath)) {
                return field;
            }
        }
        return null;
    }

    public int indexOf(Field field) {
        final int count = mFieldList.size();
        for (int index = 0; index < count; index++) {
            if (field == mFieldList.get(index)) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public int getViewTypeCount() {
        return mFieldTypes == null ? 1 : mFieldTypes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position >= 0 && position < mFieldList.size() ? mFieldList.get(position).getFieldType() : super.getItemViewType(position);
    }

    public void updateSections() {
        boolean isSectionValid = true;
        for (int index = mFieldList.size() - 1; index >= 0; index--) {
            Field field = mFieldList.get(index);
            if (field.isSectionHeader()) {
                field.setValid(isSectionValid);
                isSectionValid = true;
                field.notifyDataChanged(getViewByIndex(index));
            } else {
                isSectionValid = isSectionValid && field.isValid();
            }
        }
    }

    private View getViewByIndex(int index) {
        int headersCount = 0;
        if (mAdapterView instanceof ListView) {
            headersCount = ((ListView) mAdapterView).getHeaderViewsCount();
        }
        return mAdapterView.getChildAt(index - mAdapterView.getFirstVisiblePosition() + headersCount);
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        for (Field field : mFieldList) {
            if (field.getJsonPath() != null) {
                put(result, field.getJsonPath(), field.getResult());
            }
        }
        return result;
    }

    public boolean isValid() {
        boolean out = true;
        for (Field field : mFieldList) {
            out = out && field.isValid();
        }
        return out;
    }

    public boolean validate() {
        boolean out = true;
        for (Field field : mFieldList) {
            boolean before = out;
            out = out & field.validate();
            if (before != out) {
                requestFocus(field);
            }
        }
        return out;
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

    public void onSaveInstanceState(Bundle outState) {
        for (Field field : mFieldList) {
            field.onSaveInstanceState(outState);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (Field field : mFieldList) {
            field.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressLint("NewApi")
    public void requestFocus(Field field) {
        int index = indexOf(field);
        if (index != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO && mAdapterView instanceof AbsListView) {
                ((AbsListView) mAdapterView).smoothScrollToPositionFromTop(index, 0);
            } else {
                mAdapterView.setSelection(index);
                mAdapterView.setSelection(index);
            }
        }
    }
}
