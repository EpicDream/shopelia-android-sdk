package com.shopelia.android;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.Toast;

import com.shopelia.android.SignInFragment.OnSignInListener;
import com.shopelia.android.SignUpFragment.OnSignUpListener;
import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.analytics.AnalyticsBuilder;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.config.Config;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.shopelia.android.model.Vendor;
import com.shopelia.android.remote.api.CommandHandler;
import com.shopelia.android.remote.api.CommandHandler.CallbackAdapter;
import com.shopelia.android.remote.api.CommandHandler.ErrorInflater;
import com.shopelia.android.remote.api.UserCommandHandler;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.Tax;
import com.shopelia.android.widget.FormListFooter;
import com.shopelia.android.widget.FormListHeader;
import com.shopelia.android.widget.ProductSheetWrapper;
import com.shopelia.android.widget.ValidationButton;
import com.turbomanage.httpclient.HttpResponse;

public class PrepareOrderActivity extends ShopeliaActivity implements OnSignUpListener, OnSignInListener {

    /**
     * Url of the product to purchase
     */
    public static final String EXTRA_PRODUCT_URL = Config.EXTRA_PREFIX + "PRODUCT_URL";

    /**
     * A resource ID or {@link Uri} representing the image of product to
     * purchase
     */
    public static final String EXTRA_PRODUCT_IMAGE = Config.EXTRA_PREFIX + "PRODUCT_IMAGE";

    /**
     * Title of the product to purchase
     */
    public static final String EXTRA_PRODUCT_TITLE = Config.EXTRA_PREFIX + "PRODUCT_TITLE";

    /**
     * Description of the product to purchase
     */
    public static final String EXTRA_PRODUCT_DESCRIPTION = Config.EXTRA_PREFIX + "PRODUCT_DESCRIPTION";

    /**
     * The {@link Vendor} of the product to purchase
     */
    public static final String EXTRA_VENDOR = Config.EXTRA_PREFIX + "VENDOR";

    /**
     * The price of the product to purchase
     */
    public static final String EXTRA_PRICE = Config.EXTRA_PREFIX + "PRICE";

    /**
     * The shipping fees of the product to purchase
     */
    public static final String EXTRA_SHIPPING_PRICE = Config.EXTRA_PREFIX + "SHIPPING_FEES";

    /**
     * The shipping info of the product to purchase
     */
    public static final String EXTRA_SHIPPING_INFO = Config.EXTRA_PREFIX + "SHIPPING_INFO";

    /**
     * A {@link Tax} object
     */
    public static final String EXTRA_TAX = Config.EXTRA_PREFIX + "TAX";

    /**
     * The {@link Currency} of the price
     */
    public static final String EXTRA_CURRENCY = Config.EXTRA_PREFIX + "CURRENCY";

    private static final int REQUEST_ADD_PAYMENT_CARD = 0x0113;
    private static final int REQUEST_CREATE_PINCODE = 0x3110;
    private static final int REQUEST_AUTH_PINCODE = 0x0216;

    private SignInFragment mSignInFragment = new SignInFragment();
    private SignUpFragment mSignUpFragment = new SignUpFragment();
    private ScrollView mScrollView;

    // Cache
    private String mCachedPincode = null;

    private Map<Class<?>, Fragment.SavedState> mSavedStates = new HashMap<Class<?>, Fragment.SavedState>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getIntent().putExtra(EXTRA_INIT_ORDER, true);
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.shopelia_prepare_order_activity);
        mScrollView = (ScrollView) findViewById(R.id.scrollview);
        if (savedInstanceState == null) {
            if (!UserManager.get(this).isLogged()) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.fragment_container, mSignUpFragment);
                ft.commit();
            } else {
                Intent intent = new Intent(this, PincodeActivity.class);
                intent.putExtra(PincodeActivity.EXTRA_CREATE_PINCODE, false);
                startActivityForResult(intent, REQUEST_AUTH_PINCODE);
            }
        }
        new FormListHeader(this).setView(findViewById(R.id.header));
        new FormListFooter(this).setView(findViewById(R.id.footer));
        new ProductSheetWrapper(findViewById(R.id.header).findViewById(R.id.product_sheet), getIntent().getExtras());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (resultCode == ShopeliaActivity.RESULT_LOGOUT) {
            if (fragment == mSignUpFragment) {
                switchFragments(mSignInFragment, mSignUpFragment);
            } else if (fragment == null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.fragment_container, mSignInFragment);
                ft.commit();
            }
            return;
        }
        switch (requestCode) {
            case REQUEST_CHECKOUT:
                finish();
                return;
            case REQUEST_CREATE_PINCODE:
                if (resultCode == RESULT_OK) {
                    final Order order = getOrder();
                    order.user.pincode = data.getStringExtra(PincodeActivity.EXTRA_PINCODE);
                    mCachedPincode = order.user.pincode;
                    createAccount();
                    track(Analytics.Events.Steps.SignUp.SIGNING_UP,
                            AnalyticsBuilder.prepareStepPackage(this, Analytics.Properties.Steps.SigningUp.PINCODE));
                } else {
                    track(Analytics.Events.Steps.SignUp.SIGNING_UP,
                            AnalyticsBuilder.prepareStepPackage(this, Analytics.Properties.Steps.SigningUp.PINCODE_CANCEL));
                }
                break;
            case REQUEST_AUTH_PINCODE:
                if (resultCode == RESULT_OK) {
                    checkoutOrder(getOrder());
                } else {
                    finish();
                }
                break;
            case REQUEST_ADD_PAYMENT_CARD:
                if (resultCode == RESULT_OK) {
                    sendPaymentInformations((PaymentCard) data.getParcelableExtra(AddPaymentCardActivity.EXTRA_PAYMENT_CARD), getOrder());
                } else {
                    finish();
                }
                break;
            default:
                if (fragment != null) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }
    }

    @Override
    public void onSignUp(JSONObject result) {
        try {
            Log.d(null, result.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setOrder(Order.inflate(result));
        if (TextUtils.isEmpty(mCachedPincode)) {
            Intent intent = new Intent(this, PincodeActivity.class);
            intent.putExtra(PincodeActivity.EXTRA_CREATE_PINCODE, true);
            startActivityForResult(intent, REQUEST_CREATE_PINCODE);
        } else {
            getOrder().user.pincode = mCachedPincode;
            createAccount();
        }
    }

    private void createAccount() {
        final Order order = getOrder();
        startWaiting(getString(R.string.shopelia_form_main_waiting), true, true);
        new UserCommandHandler(this, new CommandHandler.CallbackAdapter() {

            @Override
            public void onAccountCreationSucceed(final User user, Address address) {
                super.onAccountCreationSucceed(user, address);
                track(Analytics.Events.Steps.SignUp.END);
                stopWaiting();
                UserManager.get(PrepareOrderActivity.this).login(user);
                order.user = user;
                checkoutOrder(order);
            }

            @Override
            public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
                super.onError(step, httpResponse, response, e);
                stopWaiting();
                if (e != null) {
                    Toast.makeText(PrepareOrderActivity.this, R.string.shopelia_error_network_error, Toast.LENGTH_LONG).show();
                } else {
                    mSignUpFragment.onCreateAccountError(response);
                }
            }

        }).createAccount(order.user, order.address, order.card);
    }

    private void sendPaymentInformations(PaymentCard card, final Order order) {
        new UserCommandHandler(PrepareOrderActivity.this, new CallbackAdapter() {
            @Override
            public void onPaymentInformationSent(PaymentCard paymentInformation) {
                super.onPaymentInformationSent(paymentInformation);
                User user = UserManager.get(PrepareOrderActivity.this).getUser();
                user.paymentCards.add(paymentInformation);
                UserManager.get(PrepareOrderActivity.this).login(user);
                checkoutOrder(order);
            }
        }).sendPaymentInformation(getOrder().user, card);
    }

    private void checkoutOrder(Order order) {
        order.product.productPrice = getIntent().getFloatExtra(EXTRA_PRICE, 0);
        order.product.deliveryPrice = getIntent().getFloatExtra(EXTRA_SHIPPING_PRICE, 0);
        order.product.shippingExtra = getIntent().getStringExtra(EXTRA_SHIPPING_INFO);
        order.product.url = getIntent().getStringExtra(EXTRA_PRODUCT_URL);
        order.product.name = getIntent().getStringExtra(EXTRA_PRODUCT_TITLE);
        order.product.image = getIntent().getParcelableExtra(EXTRA_PRODUCT_IMAGE);
        order.product.currency = Currency.EUR;
        order.product.tax = Tax.ATI;
        order.product.description = getIntent().getStringExtra(EXTRA_PRODUCT_DESCRIPTION);

        // TODO REMOVE THIS ONLY FOR TESTING
        order.product.vendor = Vendor.AMAZON;

        Bundle extras = getIntent().getExtras();
        if (extras.containsKey(PrepareOrderActivity.EXTRA_CURRENCY)) {
            order.product.currency = extras.getParcelable(PrepareOrderActivity.EXTRA_CURRENCY);
        }

        if (extras.containsKey(PrepareOrderActivity.EXTRA_VENDOR)) {
            order.product.vendor = extras.getParcelable(PrepareOrderActivity.EXTRA_VENDOR);
        }

        if (extras.containsKey(PrepareOrderActivity.EXTRA_TAX)) {
            order.product.tax = extras.getParcelable(PrepareOrderActivity.EXTRA_TAX);
        }

        order.user = UserManager.get(this).getUser();

        // TODO REMOVE THIS ONLY FOR TESTING
        order.address = order.user.addresses.get(0);

        if (order.user.paymentCards.size() > 0) {
            order.card = order.user.paymentCards.get(0);
        }
        if (order.card == null) {
            startActivityForResult(new Intent(this, AddPaymentCardActivity.class), REQUEST_ADD_PAYMENT_CARD);
        } else {
            Intent intent = new Intent(this, ProcessOrderActivity.class);
            intent.putExtra(ShopeliaActivity.EXTRA_ORDER, order);
            startActivityForResult(intent, ShopeliaActivity.REQUEST_CHECKOUT);
        }
    }

    @Override
    public String getActivityName() {
        return null;
    }

    @Override
    public void requestSignIn(Bundle arguments) {
        mSignInFragment.setArguments(arguments);
        switchFragments(mSignInFragment, mSignUpFragment);
    }

    @Override
    public void onSignIn(JSONObject result) {
        User user = User.inflate(result.optJSONObject(User.Api.USER));
        startWaiting(getString(R.string.shopelia_sign_in_waiting), true, true);
        new UserCommandHandler(this, new CallbackAdapter() {

            @Override
            public void onSignIn(User user) {
                super.onSignIn(user);
                stopWaiting();
                Order order = getOrder();
                order.user = user;
                checkoutOrder(order);
            }

            @Override
            public void onError(int step, HttpResponse httpResponse, JSONObject response, Exception e) {
                super.onError(step, httpResponse, response, e);
                stopWaiting();
                if (e != null) {
                    if (Config.INFO_LOGS_ENABLED) {
                        e.printStackTrace();
                    }
                    Toast.makeText(PrepareOrderActivity.this, R.string.shopelia_error_network_error, Toast.LENGTH_SHORT).show();
                }
                if (response != null) {
                    Toast.makeText(PrepareOrderActivity.this, response.optString(ErrorInflater.Api.ERROR), Toast.LENGTH_SHORT).show();
                }
            }

        }).signIn(user);
    }

    @Override
    public void requestSignUp() {
        switchFragments(mSignUpFragment, mSignInFragment);
    }

    @Override
    public ValidationButton getValidationButton() {
        return (ValidationButton) findViewById(R.id.footer).findViewById(R.id.validate);
    }

    @SuppressLint("NewApi")
    private void switchFragments(final ShopeliaFragment<?> inFragment, final ShopeliaFragment<?> outFragment) {
        final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.shopelia_fade_out_short);
        final Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.shopelia_fade_in_short);
        fadeOut.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mScrollView.smoothScrollTo(0, 0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    mSavedStates.put(outFragment.getClass(), fm.saveFragmentInstanceState(outFragment));
                    if (mSavedStates.containsKey(inFragment.getClass())) {
                        inFragment.setInitialSavedState(mSavedStates.get(inFragment.getClass()));
                    }
                    ft.replace(R.id.fragment_container, inFragment);
                    ft.commit();
                }
                findViewById(R.id.fragment_container).startAnimation(fadeIn);

            }
        });
        findViewById(R.id.fragment_container).startAnimation(fadeOut);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.fragment_container).getWindowToken(), 0);
    }
}
