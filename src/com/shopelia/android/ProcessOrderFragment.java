package com.shopelia.android;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.ProcessOrderFragment.OrderHandlerHolder;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.OrderState;
import com.shopelia.android.model.OrderState.State;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.OrderHandler;
import com.shopelia.android.widget.FontableTextView;
import com.shopelia.android.widget.WaitingView;

public class ProcessOrderFragment extends ShopeliaFragment<OrderHandlerHolder> implements OrderHandler.Callback {

    public interface OrderHandlerHolder {
        public OrderHandler getOrderHandler();

        public void askForConfirmation();

        public void confirm();

        public void onCheckoutSucceed();

        public void onCheckoutFailed();

    }

    private WaitingView mWaitingView;
    private FontableTextView mMessageTextView;
    private OrderHandler mOrderHandler;
    private Order mOrder;

    private String mCurrentMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_process_order_fragment, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mOrderHandler == null) {
            mOrderHandler = getContract().getOrderHandler();
        }

        if (savedInstanceState == null) {
            mOrder = getBaseActivity().getOrder();
            if (!UserManager.get(getActivity()).isLogged()) {
                mOrderHandler.createAccount(mOrder.user, mOrder.address);
            } else {
                mOrderHandler.retrieveUser(UserManager.get(getActivity()).getUser().id);
            }
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
        mWaitingView = (WaitingView) view.findViewById(R.id.waitingView);
        mMessageTextView = (FontableTextView) view.findViewById(R.id.message);
        mWaitingView.start();
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(getString(R.string.shopelia_waiting_shopelia_is_preparing_your_order, mOrder.product.vendor.getName()));
    }

    @Override
    public void onAccountCreationSucceed(User user, Address address) {
        mOrderHandler.order(mOrder);
        mOrderHandler.sendPaymentInformation(user, mOrder.card);
    }

    @Override
    public void onPaymentInformationSent(PaymentCard paymentInformation) {
        if (mOrderHandler.canConfirm()) {
            getContract().askForConfirmation();
        }
    }

    @Override
    public void onError(int step, JSONObject response, Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
        if (response != null) {
            Log.w(null, response.toString());
        }
        if (!isDetached()) {
            mWaitingView.setProgressColor(getResources().getColor(R.color.shopelia_red));
        }
        mWaitingView.pause();
        mOrderHandler.cancel();
    }

    @Override
    public void onOrderBegin(Order order) {

    }

    @Override
    public void onOrderStateUpdate(OrderState newState) {
        Log.d(null, "NEW STATE = " + newState.uuid + " " + newState.message + " " + newState.state);
        if (newState.state == State.ERROR) {
            mWaitingView.setProgressColor(getResources().getColor(R.color.shopelia_red));
            mWaitingView.pause();
            mOrderHandler.stopOrderForError();
        }

        if (!TextUtils.isEmpty(newState.message)) {
            if (newState.message.startsWith("expect_")) {
                try {
                    mWaitingView.setTotalSteps(Integer.parseInt(newState.message.substring(7)));
                } catch (NumberFormatException e) {
                    // Do nothing, actually not possible
                }
            } else if (!TextUtils.equals(newState.message, mCurrentMessage)) {
                mCurrentMessage = newState.message;
                mWaitingView.newStep(mCurrentMessage);

                Pattern p = Pattern.compile("$(.*)_([0-9]+)$");
                Matcher m = p.matcher(mCurrentMessage);
                if (m.find()) {
                    mMessageTextView.setText(m.group(1));
                } else {
                    mMessageTextView.setText(mCurrentMessage);
                }
            }
        }

        if (mOrderHandler.canConfirm()) {
            getContract().askForConfirmation();
        }

    }

    @Override
    public void onOrderConfirmation(boolean succeed) {

    }

    @Override
    public void onUserRetrieved(User user) {
        // TODO : Just for beta version
        mOrder.card = user.paymentCards.get(0);
        mOrder.address = user.addresses.get(0);
        mOrderHandler.order(mOrder);
    }

}
