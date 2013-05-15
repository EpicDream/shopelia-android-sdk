package com.shopelia.android;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
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
import com.shopelia.android.remote.api.Command;
import com.shopelia.android.remote.api.OrderHandler;
import com.shopelia.android.remote.api.ShopeliaRestClient;
import com.shopelia.android.widget.FontableTextView;
import com.shopelia.android.widget.WaitingView;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class ProcessOrderFragment extends ShopeliaFragment<OrderHandlerHolder> implements OrderHandler.Callback {

    public interface OrderHandlerHolder {
        public OrderHandler getOrderHandler();

        public void askForConfirmation();

        public void confirm();

        public void onCheckoutSucceed();

        public void onCheckoutFailed();

    }

    private static final int RESULT_CHECK_PINCODE = 0xbeef;
    private static final int RESULT_CREATE_PINCODE = 0xcafe;

    private WaitingView mWaitingView;
    private FontableTextView mMessageTextView;
    private OrderHandler mOrderHandler;
    private Order mOrder;
    private String mCurrentMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getView() != null) {
            return getView();
        }
        return inflater.inflate(R.layout.shopelia_process_order_fragment, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mOrderHandler == null) {
            mOrderHandler = getContract().getOrderHandler();
        }

        mOrder = getBaseActivity().getOrder();
        if (savedInstanceState == null) {

            if (mOrder.user == null) {
                mOrder.user = UserManager.get(getActivity()).getUser();
            }

            if (mOrder.user != null && TextUtils.isEmpty(mOrder.user.pincode)) {
                Intent intent = new Intent(getActivity(), PincodeActivity.class);
                intent.putExtra(PincodeActivity.EXTRA_CREATE_PINCODE, true);
                startActivityForResult(intent, RESULT_CREATE_PINCODE);
            } else {
                Intent intent = new Intent(getActivity(), PincodeActivity.class);
                intent.putExtra(PincodeActivity.EXTRA_PINCODE, mOrder.user.pincode);
                intent.putExtra(PincodeActivity.EXTRA_CREATE_PINCODE, false);
                startActivityForResult(intent, RESULT_CHECK_PINCODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mOrderHandler == null) {
            mOrderHandler = getContract().getOrderHandler();
        }
        mOrderHandler.setCallback(this);
        if (resultCode == Activity.RESULT_OK) {
            if (UserManager.get(getActivity()).isLogged() && UserManager.get(getActivity()).getUser().pincode == null) {
                User user = UserManager.get(getActivity()).getUser();
                JSONObject object = new JSONObject();
                try {
                    JSONObject userObject = new JSONObject();
                    userObject.put(User.Api.PINCODE, data.getStringExtra(PincodeActivity.EXTRA_PINCODE));
                    object.put(User.Api.USER, userObject);
                    ShopeliaRestClient.put(Command.V1.Users.Retrieve(user.id), object, new AsyncCallback() {

                        @Override
                        public void onComplete(HttpResponse httpResponse) {
                            // Do not care
                        }
                    });
                } catch (JSONException e) {
                    // Do not care
                }
            }
            if (requestCode == RESULT_CREATE_PINCODE) {
                mOrder.user.pincode = data.getStringExtra(PincodeActivity.EXTRA_PINCODE);
            }
            mWaitingView.start();
            if (!UserManager.get(getActivity()).isLogged()) {
                mOrderHandler.createAccount(mOrder.user, mOrder.address);
            } else {
                mOrderHandler.retrieveUser(UserManager.get(getActivity()).getUser().id);
            }
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOrderHandler != null) {
            mOrderHandler.resume();
        } else {
            // If the handler is dead here, the order is probably dead too. We
            // just need to notify the user and quit this activity
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOrderHandler != null) {
            mOrderHandler.pause();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOrder = getBaseActivity().getOrder();
        mWaitingView = (WaitingView) view.findViewById(R.id.waitingView);
        mMessageTextView = (FontableTextView) view.findViewById(R.id.message);
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
            if (getActivity() != null) {
                mWaitingView.setProgressColor(getResources().getColor(R.color.shopelia_red));
            }
            mWaitingView.pause();
            mOrderHandler.stopOrderForError();
        }

        if (!TextUtils.isEmpty(newState.message)) {
            Pattern p1 = Pattern.compile("^expect_([0-9]+)$");
            Matcher m1 = p1.matcher(newState.message);
            if (m1.find()) {
                mWaitingView.setTotalSteps(Integer.parseInt(m1.group(1)));
            } else if (!TextUtils.equals(newState.message, mCurrentMessage)) {
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
        // TODO : Just for beta version
        mOrder.card = user.paymentCards.get(0);
        mOrder.address = user.addresses.get(0);
        mOrderHandler.order(mOrder);
    }

}
