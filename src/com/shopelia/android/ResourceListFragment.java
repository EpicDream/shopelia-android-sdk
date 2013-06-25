package com.shopelia.android;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
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
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.TextButtonItem;

public class ResourceListFragment extends ShopeliaFragment<OnItemSelectedListener> {

    public static final int REQUEST_ADD = 0x100;

    private String mResourceIdentifier;
    private int mOptions = ResourceListActivity.OPTION_SELECT;
    private ListView mListView;
    private ModelAdapterFactory mFactory;
    private List<? extends BaseModel> mList;

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
    protected void onCreateShopeliaActionBar(ActionBar actionBar) {
        super.onCreateShopeliaActionBar(actionBar);
        actionBar.clear();
        actionBar.addItem(new TextButtonItem(R.id.shopelia_action_bar_add, getActivity(), R.string.shopelia_action_bar_add));
        actionBar.commit();
    }

    @Override
    protected void onActionItemSelected(Item item) {
        super.onActionItemSelected(item);
        ModelAdapterFactory factory = getFactory();
        if (item.getId() == R.id.shopelia_action_bar_add) {
            startActivityForResult(factory.getAddRequestIntent(getActivity()), REQUEST_ADD);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD && resultCode == Activity.RESULT_OK) {
            ModelAdapterFactory factory = getFactory();
            BaseModelAdapter<? extends BaseModel> adapter = factory.getAdapter(getActivity());
            mList = factory.getListFromUser(com.shopelia.android.manager.UserManager.get(getActivity()).getUser());
            adapter.setContent(mList);
            mListView.setAdapter(adapter);
        }
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
        mList = list;
        BaseModelAdapter<? extends BaseModel> adapter = factory.getAdapter(getActivity());
        adapter.setContent(list);
        adapter.setOptions(mOptions);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(mOnItemClickListener);
    }

    public ModelAdapterFactory getFactory() {
        ModelAdapterFactory factory = mFactory;
        List<? extends BaseModel> list = null;
        if (factory == null) {
            if (getArguments().containsKey(ResourceListActivity.EXTRA_RESOURCE)) {
                factory = ModelAdapterFactory.getInstance(getArguments().getString(ResourceListActivity.EXTRA_RESOURCE));
                list = factory.getListFromUser(com.shopelia.android.manager.UserManager.get(getActivity()).getUser());
            } else if (getArguments().containsKey(ResourceListActivity.EXTRA_LIST)) {
                list = getArguments().getParcelableArrayList(ResourceListActivity.EXTRA_LIST);
                factory = ModelAdapterFactory.getInstance(list.get(0).getClass());
            }
        }
        return factory;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

        }

    };

}
