package com.shopelia.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopelia.android.SinglePaymentCardFragment.OnPaymentCardChangeListener;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;

public class SinglePaymentCardFragment extends ShopeliaFragment<OnPaymentCardChangeListener> {

    public interface OnPaymentCardChangeListener {
        public void onPaymentCardChange(PaymentCard card);
    }

    protected static final String ARGS_PAYMENT_CARD = "args:payment_card";

    protected static final int REQUEST_SELECT_PAYMENT_CARD = 0x101;
    protected static final int REQUEST_ADD_PAYMENT_CARD = 0x102;

    private PaymentCard mCard;

    public static SinglePaymentCardFragment newInstance(PaymentCard card) {
        SinglePaymentCardFragment instance = new SinglePaymentCardFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARGS_PAYMENT_CARD, card);
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCard = getArguments().getParcelable(ARGS_PAYMENT_CARD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_confirmation_payment_card, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refresh();
    }

    protected void refresh() {
        if (mCard != null && !mCard.isExpired()) {
            findViewById(R.id.display_card).setVisibility(View.VISIBLE);
            findViewById(R.id.add_card_button).setVisibility(View.GONE);
            StringBuilder number = new StringBuilder(mCard.number);
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
            findViewById(R.id.payment_card_edit).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(getActivity(), ResourceListActivity.class);
                    intent.putExtra(ResourceListActivity.EXTRA_RESOURCE, PaymentCard.IDENTIFIER);
                    intent.putExtra(ResourceListActivity.EXTRA_OPTIONS, ResourceListActivity.OPTION_ALL);
                    intent.putExtra(ResourceListActivity.EXTRA_DEFAULT_ITEM, mCard.id);
                    startActivityForResult(intent, REQUEST_SELECT_PAYMENT_CARD);
                }
            });
        } else {
            findViewById(R.id.display_card).setVisibility(View.GONE);
            findViewById(R.id.add_card_button).setVisibility(View.VISIBLE);
            findViewById(R.id.reason, TextView.class).setText(
                    getText(mCard == null ? R.string.shopelia_confirmation_you_have_no_card : R.string.shopelia_confirmation_card_expired));
            findViewById(R.id.add_card_button).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AddPaymentCardActivity.class);
                    intent.putExtra(AddPaymentCardActivity.EXTRA_REQUIRED, true);
                    startActivityForResult(intent, REQUEST_ADD_PAYMENT_CARD);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ADD_PAYMENT_CARD:
                if (resultCode == Activity.RESULT_OK) {
                    mCard = data.getParcelableExtra(AddPaymentCardActivity.EXTRA_PAYMENT_CARD);
                    getContract().onPaymentCardChange(mCard);
                }
                break;
            case REQUEST_SELECT_PAYMENT_CARD: {
                if (resultCode == Activity.RESULT_OK) {
                    mCard = data.getParcelableExtra(ResourceListActivity.EXTRA_SELECTED_ITEM);
                    getContract().onPaymentCardChange(mCard);
                } else {
                    User user = UserManager.get(getActivity()).getUser();
                    PaymentCard card = null;
                    for (PaymentCard item : user.paymentCards) {
                        if (getOrder().card.id == item.id) {
                            card = item;
                            break;
                        }
                    }
                    if (card == null) {
                        card = user.getDefaultPaymentCard();
                    }
                    mCard = card;
                    getContract().onPaymentCardChange(mCard);
                }
                break;
            }
        }
        refresh();
    }

}
