package com.shopelia.android;

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

import com.shopelia.android.SignUpFragment.OnSignUpListener;
import com.shopelia.android.adapter.FormAdapter;
import com.shopelia.android.adapter.form.AddressField;
import com.shopelia.android.adapter.form.EmailField;
import com.shopelia.android.adapter.form.HeaderField;
import com.shopelia.android.adapter.form.PaymentCardField;
import com.shopelia.android.adapter.form.PhoneField;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.User;
import com.shopelia.android.widget.FormListFooter;
import com.shopelia.android.widget.FormListHeader;

public class SignUpFragment extends ShopeliaFragment<OnSignUpListener> {

    public interface OnSignUpListener {
        public void onSignUp(JSONObject result);
    }

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

        mAdapter = new FormAdapter(mListView);

        //@formatter:off
        mAdapter
            
            /*
             * User informations
             */
            .add(new HeaderField(getActivity(), R.string.shopelia_form_main_personal_info, R.drawable.shopelia_user).displayLock())
            .add(new PhoneField(getActivity(), null, R.string.shopelia_form_main_phone).setJsonPath(Order.Api.USER, User.Api.PHONE).mandatory())
            .add(new EmailField(getActivity(), null, R.string.shopelia_form_main_email).setJsonPath(Order.Api.USER, User.Api.EMAIL).mandatory())
            
            /*
             * Shipment details
             */
            .add(new HeaderField(getActivity(), R.string.shopelia_form_main_shipping_address, R.drawable.shopelia_pin))
            .add(new AddressField(getActivity(), R.string.shopelia_form_main_address).setJsonPath(Order.Api.ADDRESS))
            
            /*
             * Payment methods
             */
            .add(new HeaderField(getActivity(), R.string.shopelia_form_main_payment_method, R.drawable.shopelia_card).displayLock())
            .add(new PaymentCardField(getActivity(), R.string.shopelia_form_main_card_number).setJsonPath(Order.Api.PAYMENT_CARD))
            
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
                getContract().onSignUp(result);
            }
        }
    };

}
