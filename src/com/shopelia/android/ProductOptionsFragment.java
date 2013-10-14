package com.shopelia.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.model.Option;
import com.shopelia.android.model.Options;
import com.shopelia.android.model.Product;
import com.shopelia.android.widget.AsyncImageView;

public class ProductOptionsFragment extends ShopeliaFragment<Void> {

    public static final class OnOptionsChanged {
        public final int lastChange;
        public final Option[] options;

        public OnOptionsChanged(int index, Option[] options) {
            this.options = options;
            lastChange = index;
        }
    }

    public static final String TAG = "ProductOptions";

    private List<OptionsItem> mOptionsItems;
    private ViewGroup mOptionsContainer;
    private boolean mIsLoading = true;
    private boolean mIsRefreshing = false;
    private Option[] mOptions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_product_options_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOptionsContainer = findViewById(R.id.options_container);
        Product product = (Product) getActivityEventBus().getStickyEvent(Product.class);
        refreshUi(product);
    }

    private void refreshUi(Product product) {
        mIsRefreshing = true;
        mIsLoading = getBaseActivity().isInWaitingMode();
        if (mOptionsItems == null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final int size = product.versions.getOptionsCount();
            mOptionsItems = new ArrayList<ProductOptionsFragment.OptionsItem>(product.versions.getOptionsCount());
            mOptionsContainer.removeAllViews();
            if (mOptions == null) {
                mOptions = new Option[size];
            }
            for (int index = 0; index < size; index++) {
                OptionsItem oi = new OptionsItem(index);
                mOptionsItems.add(oi);
                View v = oi.inflate(inflater, mOptionsContainer);
                oi.attachView(v);
                mOptionsContainer.addView(v);
            }
        }
        final int size = product.versions.getOptionsCount();
        for (int index = 0; index < size; index++) {
            mOptionsItems.get(index).refreshUi(product, product.versions.getOptions(index));
        }
        mIsRefreshing = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivityEventBus().registerSticky(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivityEventBus().unregister(this);
    }

    private class OptionsItem {

        private int mIndex = 0;
        private View mView;

        private Spinner mSelector;
        private TextView mOptionLabel;
        private OptionsAdapter mAdapter;

        public OptionsItem(int index) {
            mIndex = index;
        }

        public View inflate(LayoutInflater inflater, ViewGroup container) {
            return inflater.inflate(R.layout.shopelia_product_option_item_fragment, container, false);
        }

        public void attachView(View v) {
            mView = v;
            mSelector = findViewById(R.id.options_selector);
            mOptionLabel = findViewById(R.id.option_label);
            mAdapter = new OptionsAdapter(mView.getContext());
            mSelector.setAdapter(mAdapter);
            mSelector.setOnItemSelectedListener(mAdapter);
        }

        public <T extends View> T findViewById(int id) {
            return (T) mView.findViewById(id);
        }

        public void refreshUi(Product product, Options options) {
            ArrayAdapter<Option> adapter = new ArrayAdapter<Option>(mView.getContext(), android.R.layout.simple_dropdown_item_1line);
            for (Option option : options) {
                adapter.add(option);
            }
            mAdapter.add(options);
            mOptionLabel.setText(getResources().getString(R.string.shopelia_product_options_option_pattern, (mIndex + 1)));
            if (mOptions[mIndex] == null) {
                setCurrentOption(mIndex, options.get(0));
            }
            int currentSelection = mSelector.getSelectedItemPosition();
            if (currentSelection >= 0) {
                mOptions[mIndex] = product.getCurrentVersion().getOptions()[mIndex];
                int indexOf = mAdapter.indexOf(product.getCurrentVersion().getOptions()[mIndex]);
                if (indexOf != currentSelection) {
                    mSelector.setSelection(indexOf);
                }
            }
            if (!product.getCurrentVersion().getOptions()[mIndex].equals(mOptions[mIndex])) {
                mOptions[mIndex] = product.getCurrentVersion().getOptions()[mIndex];
                mSelector.setSelection(mAdapter.indexOf(product.getCurrentVersion().getOptions()[mIndex]));
            }
        }

        private class OptionsAdapter extends BaseAdapter implements OnItemSelectedListener {

            private List<Option> mOptions = new ArrayList<Option>();
            private LayoutInflater mInflater;

            public OptionsAdapter(Context context) {
                mInflater = LayoutInflater.from(context);
            }

            public void add(Options options) {
                if (options.size() != mOptions.size()) {
                    for (Option option : options) {
                        if (!mOptions.contains(option)) {
                            mOptions.add(option);
                        }
                    }
                    notifyDataSetChanged();
                }
            }

            public int indexOf(Option option) {
                return mOptions.indexOf(option);
            }

            @Override
            public int getCount() {
                return mOptions.size() + (mIsLoading ? 1 : 0);
            }

            @Override
            public Object getItem(int position) {
                if (position < mOptions.size()) {
                    return mOptions.get(position);
                } else {
                    return null;
                }
            }

            @Override
            public long getItemId(int position) {
                if (position < mOptions.size()) {
                    return mOptions.get(position).hashCode();
                } else {
                    return 0;
                }
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.shopelia_product_option_list_item, parent, false);
                    ViewHolder holder = new ViewHolder();
                    holder.label = (TextView) convertView.findViewById(R.id.label);
                    holder.image = (AsyncImageView) convertView.findViewById(R.id.image);
                    holder.loading = convertView.findViewById(R.id.loading);
                    convertView.setTag(holder);
                }
                ViewHolder holder = (ViewHolder) convertView.getTag();

                if (position < mOptions.size()) {
                    Option option = mOptions.get(position);
                    if (option.isImage()) {
                        holder.image.setVisibility(View.VISIBLE);
                        holder.label.setVisibility(View.GONE);
                        holder.image.setUrl(option.src);
                    } else {
                        holder.image.setVisibility(View.GONE);
                        holder.label.setVisibility(View.VISIBLE);
                        holder.label.setText(option.text);
                    }
                    holder.loading.setVisibility(View.GONE);
                } else {
                    holder.image.setVisibility(View.GONE);
                    holder.label.setVisibility(View.GONE);
                    holder.loading.setVisibility(View.VISIBLE);
                }
                return convertView;
            }

            private class ViewHolder {
                public TextView label;
                public AsyncImageView image;
                public View loading;
            }

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (position >= mOptions.size()) {
                    mSelector.setSelection(0);
                }
                setCurrentOption(mIndex, mOptions.get(mSelector.getSelectedItemPosition()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }

        }
    }

    public void setCurrentOption(int index, Option option) {
        new Exception().printStackTrace();
        if (mIsRefreshing) {
            return;
        }
        mOptions[index] = option;
        boolean canSend = true;
        for (Option o : mOptions) {
            canSend = o != null;
            if (!canSend) {
                break;
            }
        }
        if (canSend) {
            getActivityEventBus().post(new OnOptionsChanged(index, mOptions));
        }
    }

    // Events

    public void onEventMainThread(Product product) {
        refreshUi(product);
    }

    // Spinners

}
