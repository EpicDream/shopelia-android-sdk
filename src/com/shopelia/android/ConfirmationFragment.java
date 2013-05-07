package com.shopelia.android;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shopelia.android.ProcessOrderFragment.OrderHandlerHolder;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.drawable.TicketDrawable;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.OrderState;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.OrderHandler;

public class ConfirmationFragment extends ShopeliaFragment<OrderHandlerHolder> implements OrderHandler.Callback {

    private Order mOrder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getContract().getOrderHandler().setCallback(this);
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

    @SuppressWarnings("unchecked")
    private <T extends View> T findViewById(int id) {
        return (T) getView().findViewById(id);
    }

    private <T extends View> T findViewById(int id, Class<T> clazz) {
        return (T) findViewById(id);
    }

    private OnClickListener mOnConfirmClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            getContract().getOrderHandler().confirm();
        }
    };

    @Override
    public void onAccountCreationSucceed(User user, Address address) {
        // Useless
    }

    @Override
    public void onPaymentInformationSent(PaymentCard paymentInformation) {
        // Useless
    }

    @Override
    public void onOrderBegin(Order order) {
        // Useless
    }

    @Override
    public void onOrderStateUpdate(OrderState newState) {
        // Useless
    }

    @Override
    public void onError(int step, JSONObject response, Exception e) {

    }

    @Override
    public void onOrderConfirmation(boolean succeed) {
        getContract().getOrderHandler().done();
        if (succeed) {
            getContract().onCheckoutSucceed();
        } else {
            getContract().onCheckoutFailed();
        }
    }

    // ////////////////////////////////////////////////////////////////
    //
    // UI SETUP
    //
    // ////////////////////////////////////////////////////////////////

    private void setupUi() {
        setupAddressUi();
        setupPaymentCardUi();
        setupPriceUi();
        setupProductUi();
        setupUserUi();
    }

    private void setupProductUi() {
        findViewById(R.id.product_name, TextView.class).setText(mOrder.product.name);
        findViewById(R.id.product_description, TextView.class).setText(mOrder.product.description);
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
        findViewById(R.id.payment_card_number, TextView.class).setText(mOrder.card.number);
    }

    private void setupUserUi() {
        findViewById(R.id.user_email, TextView.class).setText(mOrder.user.email);
    }

    private void setupPriceUi() {
        findViewById(R.id.price_product_name, TextView.class).setText(mOrder.product.name);
        findViewById(R.id.price_value_no_shipping, TextView.class).setText(mOrder.product.currency.format(mOrder.state.productPrice));
        findViewById(R.id.price_value_shipping, TextView.class).setText(mOrder.product.currency.format(mOrder.state.deliveryPrice));
        findViewById(R.id.price_value_total, TextView.class).setText(mOrder.product.currency.format(mOrder.state.totalPrice));

    }

    @Override
    public void onUserRetrieved(User user) {
        // TODO Auto-generated method stub

    }

}
