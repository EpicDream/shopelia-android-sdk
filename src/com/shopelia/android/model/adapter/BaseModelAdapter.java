package com.shopelia.android.model.adapter;

import static com.shopelia.android.ResourceListActivity.OPTION_ADD;
import static com.shopelia.android.ResourceListActivity.OPTION_DELETE;
import static com.shopelia.android.ResourceListActivity.OPTION_EDIT;
import static com.shopelia.android.ResourceListActivity.OPTION_SELECT;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.shopelia.android.model.BaseModel;

public abstract class BaseModelAdapter<T extends BaseModel> extends BaseAdapter {

    public interface OnEditItemClickListener {
        public void onEditItemClick(View v, BaseModel model);
    }

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<T> mContent = new ArrayList<T>();
    private int mOptions = OPTION_SELECT;
    private OnEditItemClickListener mEditItemClickListener;
    private long mDefaultId = BaseModel.NO_ID;

    public BaseModelAdapter(Context context) {
        super();
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setOnEditItemClickListener(OnEditItemClickListener l) {
        mEditItemClickListener = l;
    }

    protected OnEditItemClickListener getOnEditItemClickListener() {
        return mEditItemClickListener;
    }

    public void setDefaultId(long id) {
        mDefaultId = id;
    }

    public long getDefaultId() {
        return mDefaultId;
    }

    protected boolean hasOnEditItemClickListener() {
        return mEditItemClickListener != null;
    }

    public void setOptions(int options) {
        mOptions = options;
    }

    public void setContent(List<?> data) {
        mContent.clear();
        List<T> list = (List<T>) data;
        for (T item : list) {
            mContent.add(item);
        }
        notifyDataSetChanged();
    }

    public abstract void setContent(JSONObject root);

    @Override
    public int getCount() {
        return mContent.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < mContent.size()) {
            return mContent.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return mContent.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = createView(mLayoutInflater, container);
        }
        bindView(convertView, mContent.get(position));
        return convertView;
    }

    public boolean isAddOptionEnabled() {
        return (mOptions & OPTION_ADD) == OPTION_ADD;
    }

    public boolean isEditOptionEnabled() {
        return (mOptions & OPTION_EDIT) == OPTION_EDIT;
    }

    public boolean isDeleteOptionEnabled() {
        return (mOptions & OPTION_DELETE) == OPTION_DELETE;
    }

    public boolean isSelectEnabled() {
        return (mOptions & OPTION_SELECT) == OPTION_SELECT;
    }

    public abstract void bindView(View v, T data);

    public abstract View createView(LayoutInflater inflater, ViewGroup container);
}
