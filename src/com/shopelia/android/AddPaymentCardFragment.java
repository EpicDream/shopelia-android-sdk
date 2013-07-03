package com.shopelia.android;

import org.json.JSONException;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.shopelia.android.AddPaymentCardFragment.OnPaymentCardAddedListener;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.remote.api.ApiHandler.CallbackAdapter;
import com.shopelia.android.remote.api.PaymentAPI;
import com.shopelia.android.widget.form.FormLinearLayout;
import com.shopelia.android.widget.form.HeaderField;
import com.shopelia.android.widget.form.SingleLinePaymentCardField;

public class AddPaymentCardFragment extends ShopeliaFragment<OnPaymentCardAddedListener> {

    public interface OnPaymentCardAddedListener {
        public void onPaymentCardAdded(PaymentCard card);
    }

    private FormLinearLayout mFormContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_add_payment_card_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFormContainer = findViewById(R.id.form);
        mFormContainer.findFieldById(R.id.header, HeaderField.class).displayLock();
        mFormContainer.findFieldById(R.id.payment_card, SingleLinePaymentCardField.class).setJsonPath(Order.Api.PAYMENT_CARD);
        mFormContainer.onCreate(savedInstanceState);

        findViewById(R.id.validate).setOnClickListener(mOnValidateClickListener);
    }

    private OnClickListener mOnValidateClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mFormContainer.validate()) {
                try {
                    PaymentCard card = PaymentCard.inflate(mFormContainer.toJson().getJSONObject(Order.Api.PAYMENT_CARD));
                    new PaymentAPI(getActivity(), new CallbackAdapter() {

                        @Override
                        public void onPaymentCardAdded(PaymentCard paymentCard) {
                            getContract().onPaymentCardAdded(paymentCard);
                        };

                        @Override
                        public void onError(int step, com.turbomanage.httpclient.HttpResponse httpResponse, org.json.JSONObject response,
                                Exception e) {

                        };

                    }).addPaymentCard(card);
                } catch (JSONException e) {

                }
            }
        }
    };

}
