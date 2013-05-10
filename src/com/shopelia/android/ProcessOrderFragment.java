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

        mOrder = getBaseActivity().getOrder();
        if (!UserManager.get(getActivity()).isLogged()) {
            mOrderHandler.createAccount(mOrder.user, mOrder.address);
        } else {
            mOrderHandler.retrieveUser(UserManager.get(getActivity()).getUser().id);
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
        mWaitingView.setProgressColor(getResources().getColor(R.color.shopelia_red));
        mWaitingView.pause();
        mOrderHandler.cancel();
    }

    @Override
    public void onOrderBegin(Order order) {
        Log.d(null, "ORDER BEGIN " + order.uuid);
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
            Pattern p1 = Pattern.compile("^expect_([0-9]+)$");
            Matcher m1 = p1.matcher(newState.message);
            if (m1.find()) {
                mWaitingView.setTotalSteps(Integer.parseInt(m1.group(1)));
            } else if (!TextUtils.equals(newState.message, mCurrentMessage)){
                mCurrentMessage = newState.message; 
                mWaitingView.newStep(mCurrentMessage);
                
                Pattern p2 = Pattern.compile("^(.*)_([0-9]+)$");
                Matcher m2 = p2.matcher(mCurrentMessage);
                if (m2.find()) {
                    mMessageTextView.setText(m2.group(1));
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
        mOrder.card = user.paymentCards.get(0);
        mOrder.address = user.addresses.get(0);
        mOrderHandler.order(mOrder);
    }

}
