package com.shopelia.android;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.shopelia.android.SingleAddressFragment.OnAddressChangeListener;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.User;

public class SingleAddressFragment extends ShopeliaFragment<OnAddressChangeListener> {

    public interface OnAddressChangeListener {
        public void onAddressChange(Address address);
    }

    protected static final String ARGS_ADDRESS = "args:address";

    protected static final int REQUEST_ADD_ADDRESS = 0x103;
    protected static final int REQUEST_SELECT_ADDRESS = 0x100;

    private Address mAddress;

    public static SingleAddressFragment newInstance(Address address) {
        SingleAddressFragment fragment = new SingleAddressFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARGS_ADDRESS, address);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAddress = getArguments().getParcelable(ARGS_ADDRESS);
        } else {
            mAddress = getOrder().address;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_confirmation_address, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refresh();
    }

    public void refresh() {
        if (mAddress != null) {
            findViewById(R.id.address_user_name, TextView.class).setText(mAddress.firstname + " " + mAddress.lastname);
            findViewById(R.id.address_address, TextView.class).setText(mAddress.address);
            findViewById(R.id.address_extras, TextView.class).setText(mAddress.extras);
            if (TextUtils.isEmpty(mAddress.extras)) {
                findViewById(R.id.address_extras).setVisibility(View.GONE);
            }
            findViewById(R.id.address_city_and_country, TextView.class).setText(
                    mAddress.zipcode + ", " + mAddress.city + ", " + mAddress.getDisplayCountry());
            String number = mAddress.phone;
            try {
                PhoneNumberUtil util = PhoneNumberUtil.getInstance();
                PhoneNumber phoneNumber = util.parse(number, Locale.getDefault().getCountry());
                number = util.format(phoneNumber, PhoneNumberFormat.NATIONAL);
            } catch (NumberParseException e) {
                e.printStackTrace();
            }

            findViewById(R.id.user_phone_number, TextView.class).setText(number);
            findViewById(R.id.address_edit).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(getActivity(), ResourceListActivity.class);
                    intent.putExtra(ResourceListActivity.EXTRA_RESOURCE, Address.IDENTIFIER);
                    intent.putExtra(ResourceListActivity.EXTRA_OPTIONS, ResourceListActivity.OPTION_ALL);
                    startActivityForResult(intent, REQUEST_SELECT_ADDRESS);
                }
            });
        } else {
            Intent intent = new Intent(getActivity(), AddAddressActivity.class);
            intent.putExtra(AddAddressActivity.EXTRA_REQUIRED, true);
            intent.putExtra(AddAddressActivity.EXTRA_MODE, AddAddressActivity.MODE_ADD);
            startActivityForResult(intent, REQUEST_ADD_ADDRESS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ADD_ADDRESS:
                if (resultCode == Activity.RESULT_OK) {
                    mAddress = data.getParcelableExtra(ResourceListActivity.EXTRA_SELECTED_ITEM);
                    getContract().onAddressChange(mAddress);
                } else {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
                break;
            case REQUEST_SELECT_ADDRESS:
                if (resultCode == Activity.RESULT_OK) {
                    mAddress = data.getParcelableExtra(ResourceListActivity.EXTRA_SELECTED_ITEM);
                    getContract().onAddressChange(mAddress);
                } else {
                    User user = UserManager.get(getActivity()).getUser();
                    Address address = null;
                    for (Address item : user.addresses) {
                        if (getOrder().address.id == item.id) {
                            address = item;
                            break;
                        }
                    }
                    if (address == null) {
                        address = user.getDefaultAddress();
                    }
                    mAddress = address;
                    getContract().onAddressChange(mAddress);
                }
                break;
        }
        refresh();
    }
}
