package com.shopelia.android.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

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

        protected Field(int type) {
            mType = type;
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

        public abstract Object getResult();

        public abstract String getJsonPath();

        public abstract boolean isValid();

        public abstract void onSaveInstanceState(Bundle outState);

        public abstract void onCreate(Bundle savedInstanceState);

    }

    private List<Field> mFieldList = new ArrayList<FormAdapter.Field>();
    private HashSet<Integer> mFieldTypes = null;
    private LayoutInflater mInflater;
    private Context mContext;

    public FormAdapter(Context context) {
        mContext = context;
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

        }
        notifyDataSetChanged();
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

    @Override
    public int getViewTypeCount() {
        return mFieldTypes == null ? 1 : mFieldTypes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mFieldList.get(position).getFieldType();
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

    }
}
