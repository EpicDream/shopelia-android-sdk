package com.shopelia.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.shopelia.android.adapter.FormAdapter;
import com.shopelia.android.adapter.form.EmailField;
import com.shopelia.android.adapter.form.HeaderField;
import com.shopelia.android.adapter.form.NameField;
import com.shopelia.android.adapter.form.PasswordField;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.widget.FormListFooter;
import com.shopelia.android.widget.FormListHeader;

public class SignUpFragment extends ShopeliaFragment<Void> {

    private ListView mListView;
    private FormAdapter mAdapter;
    private FormListFooter mFooter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_signup_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFooter = new FormListFooter(getActivity());

        mListView = (ListView) view.findViewById(R.id.form);
        mListView.addHeaderView(new FormListHeader(getActivity()).getView(), null, false);
        mListView.addFooterView(mFooter.getView(), null, false);

        mAdapter = new FormAdapter(getActivity());

        //@formatter:off
        mAdapter
            
            /*
             * First section
             */
            .add(new HeaderField("Test"))
            .add(new EmailField(null, "Email").setJsonPath("User.email"))
            .add(new PasswordField(null, "Mot de passe").setJsonPath("User.password"))
            .add(new NameField(null, "Nom").setJsonPath("User.name"))
            
            
            
            .commit(savedInstanceState);
        //@formatter:on

        mListView.setAdapter(mAdapter);

        mFooter.getView().findViewById(R.id.validate).setOnClickListener(mOnClickListener);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.onSaveInstanceState(outState);
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            if (mAdapter.validate()) {
                JSONObject result = mAdapter.toJson();
                try {
                    ((TextView) mFooter.getView().findViewById(R.id.json)).setText(result.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
