package com.shopelia.android;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.drawable.TicketDrawable;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiHandler.CallbackAdapter;
import com.shopelia.android.remote.api.OrderAPI;
import com.shopelia.android.utils.DialogHelper;
import com.shopelia.android.widget.FontableTextView;
import com.shopelia.android.widget.ProductSheetWidget;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.TextButtonItem;

public class ConfirmationFragment extends ShopeliaFragment<Void> {

    public static final int REQUEST_SELECT_ADDRESS = 0x100;
    public static final int REQUEST_SELECT_PAYMENT_CARD = 0x101;
    public static final int REQUEST_ADD_PAYMENT_CARD = 0x102;
    public static final int REQUEST_ADD_ADDRESS = 0x103;

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
            // TODO Fix the drawable for pre version
            // } else if (Build.VERSION.SDK_INT >=
            // Build.VERSION_CODES.GINGERBREAD) {
            // view.findViewById(R.id.ticket).setBackgroundDrawable(new
            // TicketDrawable(getActivity()));
        } else {
            view.findViewById(R.id.ticket).setBackgroundResource(R.drawable.shopelia_field_normal);
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
            DialogHelper.buildLogoutDialog(getActivity(), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    closeSoftKeyboard();
                    UserManager.get(getActivity()).logout();
                    getActivity().setResult(ShopeliaActivity.RESULT_LOGOUT);
                    getActivity().finish();
                }
            }, null).create().show();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ADD_ADDRESS:
                if (resultCode == Activity.RESULT_OK) {
                    getBaseActivity().getOrder().address = data.getParcelableExtra(ResourceListActivity.EXTRA_SELECTED_ITEM);
                    setupUi();
                } else {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
                break;
            case REQUEST_SELECT_ADDRESS:
                if (resultCode == Activity.RESULT_OK) {
                    getBaseActivity().getOrder().address = data.getParcelableExtra(ResourceListActivity.EXTRA_SELECTED_ITEM);
                } else {
                    User user = UserManager.get(getActivity()).getUser();
                    Address address = null;
                    for (Address item : user.addresses) {
                        if (mOrder.address.id == item.id) {
                            address = item;
                            break;
                        }
                    }
                    if (address == null) {
                        address = user.getDefaultAddress();
                    }
                    getBaseActivity().getOrder().address = address;
                }
                mOrder = getBaseActivity().getOrder();
                setupUi();
                break;
            case REQUEST_ADD_PAYMENT_CARD:
                if (resultCode == Activity.RESULT_OK) {
                    getBaseActivity().getOrder().card = data.getParcelableExtra(AddPaymentCardActivity.EXTRA_PAYMENT_CARD);
                    setupUi();
                } else {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
                break;
            case REQUEST_SELECT_PAYMENT_CARD: {
                if (resultCode == Activity.RESULT_OK) {
                    getBaseActivity().getOrder().card = data.getParcelableExtra(ResourceListActivity.EXTRA_SELECTED_ITEM);
                } else {
                    User user = UserManager.get(getActivity()).getUser();
                    PaymentCard card = null;
                    for (PaymentCard item : user.paymentCards) {
                        if (mOrder.card.id == item.id) {
                            card = item;
                            break;
                        }
                    }
                    if (card == null) {
                        card = user.getDefaultPaymentCard();
                    }
                    getBaseActivity().getOrder().card = card;
                }
                mOrder = getBaseActivity().getOrder();
                setupUi();
                break;
            }
        }

    }

    private OnClickListener mOnConfirmClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (((CheckBox) getView().findViewById(R.id.auto_cancel)).isChecked()) {
                mOrder.product.productPrice = 0;
                mOrder.product.deliveryPrice = 0;
            }

            startWaiting(getString(R.string.shopelia_confirmation_waiting, mOrder.product.merchant.name), true, false);
            new OrderAPI(getActivity(), new CallbackAdapter() {

                public void onOrderConfirmation(boolean succeed) {
                    stopWaiting();
                    UserManager.get(getActivity()).notifyCheckoutSucceed();
                    Intent intent = new Intent(getActivity(), CloseCheckoutActivity.class);
                    intent.putExtra(ShopeliaActivity.EXTRA_ORDER, mOrder);
                    getActivity().startActivityForResult(intent, ShopeliaActivity.REQUEST_CHECKOUT);
                };

                @Override
                public void onError(int step, com.turbomanage.httpclient.HttpResponse httpResponse, org.json.JSONObject response,
                        Exception e) {
                    stopWaiting();
                    Toast.makeText(getActivity(), R.string.shopelia_confirmation_error, Toast.LENGTH_LONG).show();
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
        setupPaymentCardUi();
        setupProductUi();
        setupAddressUi();
        setupPriceUi();
        setupUserUi();
    }

    private void setupProductUi() {
        //@formatter:off
        findViewById(R.id.product_sheet, ProductSheetWidget.class)
            .setProductInfo(mOrder.product)
            .refreshView();
        //@formatter:on
    }

    private void setupAddressUi() {
        if (mOrder.address != null) {
            findViewById(R.id.address_user_name, TextView.class).setText(mOrder.address.firstname + " " + mOrder.address.lastname);
            findViewById(R.id.address_address, TextView.class).setText(mOrder.address.address);
            findViewById(R.id.address_extras, TextView.class).setText(mOrder.address.extras);
            if (TextUtils.isEmpty(mOrder.address.extras)) {
                findViewById(R.id.address_extras).setVisibility(View.GONE);
            }
            findViewById(R.id.address_city_and_country, TextView.class).setText(
                    mOrder.address.zipcode + ", " + mOrder.address.city + ", " + mOrder.address.getDisplayCountry());
            String number = mOrder.address.phone;
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

    private void setupPaymentCardUi() {
        if (mOrder.card != null) {
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
            findViewById(R.id.payment_card_edit).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(getActivity(), ResourceListActivity.class);
                    intent.putExtra(ResourceListActivity.EXTRA_RESOURCE, PaymentCard.IDENTIFIER);
                    intent.putExtra(ResourceListActivity.EXTRA_OPTIONS, ResourceListActivity.OPTION_ALL);
                    intent.putExtra(ResourceListActivity.EXTRA_DEFAULT_ITEM, mOrder.card.id);
                    startActivityForResult(intent, REQUEST_SELECT_PAYMENT_CARD);
                }
            });
        } else {
            Intent intent = new Intent(getActivity(), AddPaymentCardActivity.class);
            intent.putExtra(AddPaymentCardActivity.EXTRA_REQUIRED, true);
            startActivityForResult(intent, REQUEST_ADD_PAYMENT_CARD);
        }
    }

    private void setupUserUi() {
        findViewById(R.id.user_email, TextView.class).setText(mOrder.user.email);
    }

    private void setupPriceUi() {
        findViewById(R.id.price_product_name, TextView.class).setText(mOrder.product.name);
        findViewById(R.id.price_value_no_shipping, TextView.class).setText(mOrder.product.currency.format(mOrder.product.productPrice));
        if ((int) (mOrder.product.deliveryPrice * 100) == 0) {
            FontableTextView fees = findViewById(R.id.price_value_shipping);
            fees.setText(R.string.shopelia_confirmation_free);
        } else {
            findViewById(R.id.price_value_shipping, TextView.class).setText(mOrder.product.currency.format(mOrder.product.deliveryPrice));
        }
        findViewById(R.id.price_value_total, TextView.class).setText(
                mOrder.product.currency.format(mOrder.product.productPrice + mOrder.product.deliveryPrice));
        findViewById(R.id.price_shipping_info, TextView.class).setText(mOrder.product.shippingExtra);

        // Testing purposes
        if (mOrder.user.email.equals("elarch@gmail.com") || mOrder.user.email.contains("shopelia")
                || mOrder.user.email.contains("prixing.fr")) {
            getView().findViewById(R.id.auto_cancel).setVisibility(View.VISIBLE);
        }
    }
}
