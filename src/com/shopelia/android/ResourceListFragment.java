package com.shopelia.android;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;

import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.model.BaseModel;
import com.shopelia.android.model.adapter.BaseModelAdapter;
import com.shopelia.android.model.adapter.ModelAdapterFactory;

public class ResourceListFragment extends ShopeliaFragment<OnItemSelectedListener> {

    private String mResourceIdentifier;
    private int mOptions = ResourceListActivity.OPTION_SELECT;
    private ListView mListView;

    public static ResourceListFragment newInstance(Bundle arguments) {
        ResourceListFragment f = new ResourceListFragment();
        f.setArguments(arguments);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResourceIdentifier = getArguments().getString(ResourceListActivity.EXTRA_RESOURCE);
        mOptions = getArguments().getInt(ResourceListActivity.EXTRA_OPTIONS, ResourceListActivity.OPTION_SELECT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_resource_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = findViewById(android.R.id.list);
        ModelAdapterFactory factory = null;
        List<? extends BaseModel> list = null;
        if (getArguments().containsKey(ResourceListActivity.EXTRA_RESOURCE)) {
            factory = ModelAdapterFactory.getInstance(getArguments().getString(ResourceListActivity.EXTRA_RESOURCE));
            list = factory.getListFromUser(com.shopelia.android.manager.UserManager.get(getActivity()).getUser());
        } else if (getArguments().containsKey(ResourceListActivity.EXTRA_LIST)) {
            list = getArguments().getParcelableArrayList(ResourceListActivity.EXTRA_LIST);
            factory = ModelAdapterFactory.getInstance(list.get(0).getClass());
        }
        if (factory == null) {
            getActivity().finish();
            return;
        }
        BaseModelAdapter<? extends BaseModel> adapter = factory.getAdapter(getActivity());
        adapter.setContent(list);
        adapter.setOptions(mOptions);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(mOnItemClickListener);
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

        }

    };

}
