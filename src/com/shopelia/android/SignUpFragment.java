package com.shopelia.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.shopelia.android.adapter.FormAdapter;
import com.shopelia.android.adapter.form.EditTextField;
import com.shopelia.android.adapter.form.HeaderField;
import com.shopelia.android.app.ShopeliaFragment;

public class SignUpFragment extends ShopeliaFragment<Void> {

    private ListView mListView;
    private FormAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_signup_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = (ListView) view.findViewById(R.id.form);
        mAdapter = new FormAdapter(getActivity());

        //@formatter:off
        mAdapter
            .add(new HeaderField("Test"))
            .add(new EditTextField(null, "Edit test"))
            .commit(savedInstanceState);
        //@formatter:on

        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.onSaveInstanceState(outState);
    }

}
