package com.shopelia.android;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

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
            view.findViewById(R.id.ticket).setBackground(new TicketDrawable());
        } else {
            view.findViewById(R.id.ticket).setBackgroundDrawable(new TicketDrawable());
        }
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

}
