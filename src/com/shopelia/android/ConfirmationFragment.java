package com.shopelia.android;

import org.json.JSONObject;

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

import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.drawable.TicketDrawable;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Product;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiHandler.CallbackAdapter;
import com.shopelia.android.remote.api.OrderAPI;
import com.shopelia.android.remote.api.ProductAPI;
import com.shopelia.android.remote.api.UserAPI;
import com.shopelia.android.utils.DialogHelper;
import com.shopelia.android.widget.FontableTextView;
import com.shopelia.android.widget.ProductSheetWidget;
import com.shopelia.android.widget.actionbar.ActionBar;
import com.shopelia.android.widget.actionbar.ActionBar.Item;
import com.shopelia.android.widget.actionbar.TextButtonItem;
import com.turbomanage.httpclient.HttpResponse;

public class ConfirmationFragment extends ShopeliaFragment<Void> {

    private static final int REQUEST_ADDRESS = 0x200;
    private static final int REQUEST_PAYMENT_CARD = 0x201;

    private boolean mIsOrdering = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shopelia_order_confirmation_fragment, container, false);
    }

    @SuppressLint("NewApi")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.confirm).setOnClickListener(mOnConfirmClickListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.findViewById(R.id.ticket).setBackground(new TicketDrawable(getActivity()));
        } else {
            view.findViewById(R.id.ticket).setBackgroundResource(R.drawable.shopelia_field_normal);
        }
        SingleAddressFragment.newInstance(getOrder().address).replace(getFragmentManager(), R.id.address);
        SinglePaymentCardFragment.newInstance(getOrder().card).replace(getFragmentManager(), R.id.payment_card);
        setupUi();
        if (UserManager.get(getActivity()).isAutoSignedIn()) {
            updateUser(false);
        }
        new ProductAPI(getActivity(), new CallbackAdapter() {

            @Override
            public void onProductUpdate(Product product, boolean fromNetwork) {
                super.onProductUpdate(product, fromNetwork);
                getOrder().product = product;
                findViewById(R.id.product_sheet, ProductSheetWidget.class).setProductInfo(getOrder().product, fromNetwork);
                setupUi();
            }

            @Override
            public void onProductNotAvailable(Product product) {
                super.onProductNotAvailable(product);
            }

            @Override
            public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
                super.onError(step, httpResponse, response, e);
            }

        }).getProduct(getOrder().product);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ADDRESS:
                    getOrder().address = data.getParcelableExtra(AddAddressActivity.EXTRA_ADDRESS_OBJECT);
                    SingleAddressFragment.newInstance(getOrder().address).replace(getFragmentManager(), R.id.address);
                    break;

                case REQUEST_PAYMENT_CARD:
                    getOrder().card = data.getParcelableExtra(AddPaymentCardActivity.EXTRA_PAYMENT_CARD);
                    SinglePaymentCardFragment.newInstance(getOrder().card).replace(getFragmentManager(), R.id.payment_card);
                    break;
            }
            order(false);
        }
    }

    protected void updateUser(final boolean block) {
        startWaiting(getString(R.string.shopelia_confirmation_update_user), block, false);
        new UserAPI(getActivity(), new CallbackAdapter() {

            @Override
            public void onUserUpdateDone() {
                super.onUserUpdateDone();
                User user = UserManager.get(getActivity()).getUser();
                getBaseActivity().getOrder().updateUser(user);
                setupUi();
                if (block) {
                    order(true);
                }
            }

            @Override
            public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
                super.onError(step, httpResponse, response, e);
                stopWaiting();
            }

            @Override
            public void onAuthTokenRevoked() {
                super.onAuthTokenRevoked();
                stopWaiting();
                new AuthenticateFragment().show(getFragmentManager(), AuthenticateFragment.DIALOG_NAME);
            }

        }).updateUser();
    }

    @Override
    protected void onCreateShopeliaActionBar(ActionBar actionBar) {
        super.onCreateShopeliaActionBar(actionBar);
        actionBar.clear();
        if (UserManager.get(getActivity()).isAutoSignedIn()) {
            actionBar.addItem(new TextButtonItem(R.id.shopelia_action_bar_sign_out, getActivity(), R.string.shopelia_action_bar_sign_out));
        }
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
            }, null).show();

        }
    }

    private OnClickListener mOnConfirmClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            order(false);
        }
    };

    public void order(final boolean reentrant) {
        if (mIsOrdering) {
            return;
        }
        mIsOrdering = true;
        if (getOrder().address == null) {
            Intent intent = new Intent(getActivity(), AddAddressActivity.class);
            intent.putExtra(AddAddressActivity.EXTRA_REQUIRED, true);
            intent.putExtra(AddAddressActivity.EXTRA_MODE, AddAddressActivity.MODE_ADD);
            startActivityForResult(intent, REQUEST_ADDRESS);
            mIsOrdering = false;
            return;
        }

        if (getOrder().card == null) {
            Intent intent = new Intent(getActivity(), AddPaymentCardActivity.class);
            intent.putExtra(AddPaymentCardActivity.EXTRA_REQUIRED, true);
            startActivityForResult(intent, REQUEST_PAYMENT_CARD);
            mIsOrdering = false;
            return;
        }

        getBaseActivity().startDelayedWaiting(getString(R.string.shopelia_confirmation_waiting, getOrder().product.merchant.name), true,
                false, 500);
        new OrderAPI(getActivity(), new CallbackAdapter() {

            @Override
            public void onOrderConfirmation(boolean succeed) {
                stopWaiting();
                mIsOrdering = false;
                UserManager.get(getActivity()).notifyCheckoutSucceed();
                Intent intent = new Intent(getActivity(), CloseCheckoutActivity.class);
                intent.putExtra(ShopeliaActivity.EXTRA_ORDER, getOrder());
                getActivity().startActivityForResult(intent, ShopeliaActivity.REQUEST_CHECKOUT);
            }

            @Override
            public void onInvalidOrderRequest(String message) {
                super.onInvalidOrderRequest(message);
                stopWaiting();
                mIsOrdering = false;
                if (!reentrant) {
                    updateUser(true);
                } else {
                    Toast.makeText(getActivity(), TextUtils.isEmpty(message) ? getString(R.string.shopelia_confirmation_error) : message,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(int step, com.turbomanage.httpclient.HttpResponse httpResponse, org.json.JSONObject response, Exception e) {
                stopWaiting();
                mIsOrdering = false;
                Toast.makeText(getActivity(), R.string.shopelia_confirmation_error, Toast.LENGTH_LONG).show();
            };

        }).order(getOrder(), findViewById(R.id.auto_cancel, CheckBox.class).isChecked());
    }

    // ////////////////////////////////////////////////////////////////
    //
    // UI SETUP
    //
    // ////////////////////////////////////////////////////////////////

    public void setupUi(User user) {
        if (getBaseActivity() != null) {
            getBaseActivity().getOrder().updateUser(user);
            setupUi();
        }
    }

    private void setupUi() {
        setupProductUi();
        setupPriceUi();
        setupUserUi();
        findViewById(R.id.confirm).setEnabled(getOrder().product.isValid());
    }

    private void setupProductUi() {
        //@formatter:off
       
        //@formatter:on
    }

    private void setupUserUi() {
        findViewById(R.id.user_email, TextView.class).setText(getOrder().user.email);
    }

    private void setupPriceUi() {
        findViewById(R.id.price_product_name, TextView.class).setText(getOrder().product.name);
        findViewById(R.id.price_value_no_shipping, TextView.class).setText(
                getOrder().product.currency.format(getOrder().product.productPrice));
        if ((int) (getOrder().product.deliveryPrice * 100) == 0) {
            FontableTextView fees = findViewById(R.id.price_value_shipping);
            fees.setText(R.string.shopelia_confirmation_free);
        } else {
            findViewById(R.id.price_value_shipping, TextView.class).setText(
                    getOrder().product.currency.format(getOrder().product.deliveryPrice));
        }
        findViewById(R.id.price_value_total, TextView.class).setText(
                getOrder().product.currency.format(getOrder().product.productPrice + getOrder().product.deliveryPrice));
        findViewById(R.id.price_shipping_info, TextView.class).setText(getOrder().product.shippingExtra);

        // Testing purposes
        if (getOrder().user.email.equals("elarch@gmail.com") || getOrder().user.email.contains("shopelia")
                || getOrder().user.email.contains("prixing.fr")) {
            getView().findViewById(R.id.auto_cancel).setVisibility(View.VISIBLE);
        }
    }
}
