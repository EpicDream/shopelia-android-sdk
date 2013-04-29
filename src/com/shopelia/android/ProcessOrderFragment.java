package com.shopelia.android;

import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.api.OrderHandler;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.OrderState;
import com.shopelia.android.model.OrderState.State;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;

public class ProcessOrderFragment extends ShopeliaFragment<Void> implements OrderHandler.Callback {

    private TextView mStateView;
    private OrderHandler mOrderHandler;
    private Order mOrder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_process_order_fragment, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mOrderHandler == null) {
            mOrderHandler = new OrderHandler(getActivity(), this);
        }

        mOrder = getBaseActivity().getOrder();

        if (mOrder.user.id == User.NO_ID) {
            mOrderHandler.createAccount(mOrder.user, mOrder.address);
        } else {
            mOrderHandler.order(mOrder);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mOrderHandler.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mOrderHandler.pause();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStateView = (TextView) view.findViewById(R.id.state);

    }

    @Override
    public void onAccountCreationSucceed(User user, Address address) {
        mOrderHandler.sendPaymentInformation(user, mOrder.card);
        mStateView.setText("Account created");
    }

    @Override
    public void onPaymentInformationSent(PaymentCard paymentInformation) {
        mOrderHandler.order(mOrder);
        mStateView.setText("Payment card sent");
    }

    @Override
    public void onError(int step, JSONObject response, Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
        if (response != null) {
            Log.w(null, response.toString());
        }
    }

    @Override
    public void onOrderBegin(Order order) {
        Log.d(null, "ORDER BEGIN " + order.uuid);
        mStateView.setText("Order began");
    }

    @Override
    public void onOrderStateUpdate(OrderState newState) {
        Log.d(null, "NEW STATE = " + newState.uuid + " " + newState.message + " " + newState.state);
        if (newState.state == State.ERROR) {
            mStateView.setText("Vulcain fatal error");
            mOrderHandler.stopOrderForError();
        }
    }

}
