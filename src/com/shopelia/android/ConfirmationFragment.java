package com.shopelia.android;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.drawable.TicketDrawable;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Order;
import com.shopelia.android.remote.api.CommandHandler.CallbackAdapter;
import com.shopelia.android.remote.api.OrderCommandHandler;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.TextButtonItem;

public class ConfirmationFragment extends ShopeliaFragment<Void> {

    private Order mOrder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_order_confirmation_fragment, container, false);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.confirm).setOnClickListener(mOnConfirmClickListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.findViewById(R.id.ticket).setBackground(new TicketDrawable(getActivity()));
        } else {
            view.findViewById(R.id.ticket).setBackgroundDrawable(new TicketDrawable(getActivity()));
        }
        mOrder = getBaseActivity().getOrder();
        setupUi();
    }

    @Override
    protected void onCreateShopeliaActionBar(ActionBar actionBar) {
        super.onCreateShopeliaActionBar(actionBar);
        actionBar.clear();
        actionBar.addItem(new TextButtonItem(R.id.shopelia_action_bar_sign_out, getActivity(), R.string.shopelia_action_bar_sign_out));
        actionBar.commit();
    }

    @Override
    protected void onActionItemSelected(Item item) {
        super.onActionItemSelected(item);
        if (item.getId() == R.id.shopelia_action_bar_sign_out) {
            UserManager.get(getActivity()).logout();
            getActivity().setResult(ShopeliaActivity.RESULT_LOGOUT);
            getActivity().finish();
        }
    }

    private OnClickListener mOnConfirmClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            new OrderCommandHandler(getActivity(), new CallbackAdapter() {

                public void onOrderConfirmation(boolean succeed) {
                    Intent intent = new Intent(getActivity(), CloseCheckoutActivity.class);
                    intent.putExtra(ShopeliaActivity.EXTRA_ORDER, mOrder);
                    getActivity().startActivityForResult(intent, ShopeliaActivity.REQUEST_CHECKOUT);
                };

            }).order(mOrder);
        }
    };

    // ////////////////////////////////////////////////////////////////
    //
    // UI SETUP
    //
    // ////////////////////////////////////////////////////////////////

    private void setupUi() {
        setupProductUi();
        setupAddressUi();
        setupPaymentCardUi();
        setupPriceUi();
        setupUserUi();
    }

    private void setupProductUi() {
        findViewById(R.id.product_name, TextView.class).setText(mOrder.product.name);
        findViewById(R.id.product_description, TextView.class).setText(mOrder.product.description);
        if (TextUtils.isEmpty(mOrder.product.description)) {
            findViewById(R.id.product_description, TextView.class).setVisibility(View.GONE);
        }
        findViewById(R.id.product_image, ImageView.class).setImageURI(mOrder.product.image);
        findViewById(R.id.product_vendor_icon, ImageView.class).setImageResource(mOrder.product.vendor.getImageResId());
    }

    private void setupAddressUi() {
        findViewById(R.id.address_user_name, TextView.class).setText(mOrder.user.firstName + " " + mOrder.user.lastName);
        findViewById(R.id.address_address, TextView.class).setText(mOrder.address.address);
        findViewById(R.id.address_extras, TextView.class).setText(mOrder.address.extras);
        if (TextUtils.isEmpty(mOrder.address.extras)) {
            findViewById(R.id.address_extras).setVisibility(View.GONE);
        }
        findViewById(R.id.address_city_and_country, TextView.class).setText(
                mOrder.address.zipcode + ", " + mOrder.address.city + ", " + mOrder.address.getDisplayCountry());
    }

    private void setupPaymentCardUi() {
        StringBuilder number = new StringBuilder(mOrder.card.number);
        int relativeIndex = 0;
        for (int index = 0; index < number.length(); index++) {
            if (index < number.length() - 4) {
                number.replace(index, index + 1, "*");
            }
            if (index > 0 && relativeIndex % 4 == 0) {
                number.insert(index, " ");
                index++;
                relativeIndex = 0;
            }
            relativeIndex++;
        }
        findViewById(R.id.payment_card_number, TextView.class).setText(number);
    }

    private void setupUserUi() {
        findViewById(R.id.user_email, TextView.class).setText(mOrder.user.email);
        String number = mOrder.address.phones.get(0).number;
        try {
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();
            PhoneNumber phoneNumber = util.parse(number, Locale.getDefault().getCountry());
            number = util.format(phoneNumber, PhoneNumberFormat.NATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        findViewById(R.id.user_phone_number, TextView.class).setText(number);
    }

    private void setupPriceUi() {
        findViewById(R.id.price_product_name, TextView.class).setText(mOrder.product.name);
        // findViewById(R.id.price_value_no_shipping,
        // TextView.class).setText(mOrder.product.currency.format(mOrder.state.productPrice));
        // findViewById(R.id.price_value_shipping,
        // TextView.class).setText(mOrder.product.currency.format(mOrder.state.deliveryPrice));
        // findViewById(R.id.price_value_total,
        // TextView.class).setText(mOrder.product.currency.format(mOrder.state.totalPrice));

    }

}
