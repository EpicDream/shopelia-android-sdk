package com.shopelia.android.widget;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.shopelia.android.utils.FormatUtils;

public class AutoCompletionAdapter<T> extends BaseAdapter implements Filterable {

    private int mTextViewId;
    private int mLayoutId;
    private List<T> mData;
    private List<T> mDisplayed = new ArrayList<T>();
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private PrivateFilter mFilter;

    public AutoCompletionAdapter(Context context, int layoutId, List<T> data) {
        this(context, layoutId, android.R.id.text1, data);
    }

    public AutoCompletionAdapter(Context context, int layoutId, int textViewId, List<T> data) {
        mContext = context;
        mLayoutId = layoutId;
        mTextViewId = textViewId;
        mData = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public int getCount() {
        return mDisplayed.size();
    }

    @Override
    public Object getItem(int position) {
        return mDisplayed.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mLayoutId, container, false);
            ViewHolder holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(mTextViewId);
            convertView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.textView.setText(getItem(position).toString());
        return convertView;
    }

    private static class ViewHolder {
        public TextView textView;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new PrivateFilter();
        }
        return mFilter;
    }

    private class PrivateFilter extends Filter {

        private Collator mCollator;

        public PrivateFilter() {
            mCollator = Collator.getInstance();
            mCollator.setStrength(Collator.PRIMARY);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            mDisplayed.clear();
            String compare = FormatUtils.stripAccents(constraint != null ? constraint.toString() : "").toLowerCase();
            for (T item : mData) {
                String stripped = FormatUtils.stripAccents(item.toString()).toLowerCase();
                if (stripped.contains(compare) && !TextUtils.isEmpty(stripped)) {
                    mDisplayed.add(item);
                }
            }
            results.count = mDisplayed.size();
            results.values = mDisplayed;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            notifyDataSetChanged();
        }

    }

}
