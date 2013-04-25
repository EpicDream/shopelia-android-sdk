package com.shopelia.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import com.shopelia.android.adapter.FormAdapter;
import com.shopelia.android.adapter.form.AddressField;
import com.shopelia.android.adapter.form.EmailField;
import com.shopelia.android.adapter.form.HeaderField;
import com.shopelia.android.adapter.form.PaymentCardField;
import com.shopelia.android.adapter.form.PhoneField;
import com.shopelia.android.app.HostActivity;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.config.Config;
import com.shopelia.android.model.Order;
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
             * User informations
             */
            .add(new HeaderField(getActivity(), R.string.shopelia_form_main_personal_info))
            .add(new PhoneField(getActivity(), null, R.string.shopelia_form_main_phone))
            .add(new EmailField(getActivity(), null, R.string.shopelia_form_main_email).setJsonPath("User.email"))
            
            /*
             * Shipment details
             */
            .add(new HeaderField(getActivity(), R.string.shopelia_form_main_shipping_address))
            .add(new AddressField(getActivity(), R.string.shopelia_form_main_address))
            
            /*
             * Payment methods
             */
            .add(new HeaderField(getActivity(), R.string.shopelia_form_main_payment_method))
            .add(new PaymentCardField(getActivity(), R.string.shopelia_form_main_card_number))
            
            .commit(savedInstanceState);
        //@formatter:on

        mListView.setAdapter(mAdapter);

        mFooter.getView().findViewById(R.id.validate).setOnClickListener(mOnClickListener);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAdapter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.onSaveInstanceState(outState);
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (mAdapter.validate()) {
                JSONObject result = mAdapter.toJson();
                try {
                    ((TextView) mFooter.getView().findViewById(R.id.json)).setText(result.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Order order = Order.inflate(result);
                Intent intent = new Intent(getActivity(), ProcessOrderActivity.class);
                intent.putExtra(HostActivity.EXTRA_ORDER, order);
                getActivity().startActivityForResult(intent, Config.REQUEST_ORDER);
            }
        }
    };

}
