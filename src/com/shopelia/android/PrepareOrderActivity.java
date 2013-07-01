package com.shopelia.android;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
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
import com.shopelia.android.model.Merchant;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiHandler;
import com.shopelia.android.remote.api.ApiHandler.CallbackAdapter;
import com.shopelia.android.remote.api.ApiHandler.ErrorInflater;
import com.shopelia.android.remote.api.UserAPI;
import com.shopelia.android.service.ShopeliaService;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.Tax;
import com.shopelia.android.view.animation.ResizeAnimation;
import com.shopelia.android.view.animation.ResizeAnimation.OnViewRectComputedListener;
import com.shopelia.android.widget.ExtendedFrameLayout;
import com.shopelia.android.widget.FormListFooter;
import com.shopelia.android.widget.FormListHeader;
import com.shopelia.android.widget.ProductSheetWrapper;
import com.shopelia.android.widget.ValidationButton;
import com.shopelia.android.widget.form.SingleLinePaymentCardField;
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
     * The {@link Merchant} of the product to purchase
     */
    public static final String EXTRA_MERCHANT = Config.EXTRA_PREFIX + "MERCHANT";

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

    /**
     * An email that will be pre-filled for the user
     */
    public static final String EXTRA_USER_EMAIL = Config.EXTRA_PREFIX + "USER_EMAIL";

    /**
     * An phone that will be pre-filled for the user
     */
    public static final String EXTRA_USER_PHONE = Config.EXTRA_PREFIX + "USER_PHONE";

    private static final int REQUEST_CREATE_PINCODE = 0x101;
    private static final int REQUEST_AUTH_PINCODE = 0x0102;

    private SignInFragment mSignInFragment = new SignInFragment();
    private SignUpFragment mSignUpFragment = new SignUpFragment();
    private ScrollView mScrollView;

    private int mSignInViewCount = 0;

    private boolean mCardScanned = false;

    // Cache
    private String mCachedPincode = null;

    private Map<Class<?>, Fragment.SavedState> mSavedStates = new HashMap<Class<?>, Fragment.SavedState>();

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getIntent().putExtra(EXTRA_INIT_ORDER, true);
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.shopelia_prepare_order_activity);
        mScrollView = (ScrollView) findViewById(R.id.scrollview);

        if (getIntent().getExtras() == null || !getIntent().getExtras().containsKey(EXTRA_PRODUCT_URL)) {
            finish();
            return;
        }

        if (savedInstanceState == null) {
            createSessionId(System.currentTimeMillis(), getIntent().getStringExtra(EXTRA_PRODUCT_URL));
            if (!UserManager.get(this).isLogged()) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                if (UserManager.get(this).getLoginsCount() > 0) {
                    ft.add(R.id.fragment_container, mSignInFragment, mSignInFragment.getName());
                } else {
                    ft.add(R.id.fragment_container, mSignUpFragment, mSignUpFragment.getName());
                }
                ft.commit();
            } else {
                Intent intent = new Intent(this, PincodeActivity.class);
                intent.putExtra(PincodeActivity.EXTRA_CREATE_PINCODE, false);
                startActivityForResult(intent, REQUEST_AUTH_PINCODE);
            }
        }
        startService(new Intent(ShopeliaService.ACTION));
        new FormListHeader(this).setView(findViewById(R.id.header));
        new FormListFooter(this).setView(findViewById(R.id.footer));
        new ProductSheetWrapper(findViewById(R.id.header).findViewById(R.id.product_sheet), getIntent().getExtras());
        initPhoneLayout();
    }

    private void initPhoneLayout() {
        TextView phone = (TextView) findViewById(R.id.call_shopelia);
        phone.setText(Html.fromHtml(phone.getText().toString()));
        phone.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:01.82.09.15.44"));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException e) {

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SingleLinePaymentCardField.REQUEST_CARD && resultCode == RESULT_OK) {
            mCardScanned = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        switch (requestCode) {
            case REQUEST_CHECKOUT:
                Intent result = new Intent();
                // TODO : add product in result
                setResult(resultCode, result);
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
                }
                break;
            case REQUEST_AUTH_PINCODE:
                if (resultCode == RESULT_OK) {
                    checkoutOrder(getOrder());
                } else if (resultCode == RESULT_CANCELED) {
                    finish();
                    return;
                }
                break;
            default:
                if (fragment != null) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }
        if (!UserManager.get(this).isLogged() && !isFinishing()) {
            if (fragment == null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, resultCode == RESULT_LOGOUT ? mSignInFragment : mSignUpFragment);
                ft.commit();
            }
        }
    }

    @Override
    public void onSignUp(JSONObject result) {
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
        new UserAPI(this, new ApiHandler.CallbackAdapter() {

            @Override
            public void onAccountCreationSucceed(final User user, Address address) {
                super.onAccountCreationSucceed(user, address);
                if (mCardScanned) {
                    track(Analytics.Events.AddPaymentCardMethod.CARD_SCANNED);
                } else {
                    track(Analytics.Events.AddPaymentCardMethod.CARD_NOT_SCANNED);
                }
                track(Analytics.Events.Steps.SignUp.END);
                stopWaiting();
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
        new UserAPI(PrepareOrderActivity.this, new CallbackAdapter() {
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

    private void prepareOrder(Order order) {
        order.product.productPrice = getIntent().getFloatExtra(EXTRA_PRICE, 0);
        order.product.deliveryPrice = getIntent().getFloatExtra(EXTRA_SHIPPING_PRICE, 0);
        order.product.shippingExtra = getIntent().getStringExtra(EXTRA_SHIPPING_INFO);
        order.product.url = getIntent().getStringExtra(EXTRA_PRODUCT_URL);
        order.product.name = getIntent().getStringExtra(EXTRA_PRODUCT_TITLE);
        order.product.image = getIntent().getParcelableExtra(EXTRA_PRODUCT_IMAGE);
        order.product.merchant = getIntent().getParcelableExtra(EXTRA_MERCHANT);
        order.product.currency = Currency.EUR;
        order.product.tax = Tax.ATI;
        order.product.description = getIntent().getStringExtra(EXTRA_PRODUCT_DESCRIPTION);

        Bundle extras = getIntent().getExtras();
        if (extras.containsKey(PrepareOrderActivity.EXTRA_CURRENCY)) {
            order.product.currency = extras.getParcelable(PrepareOrderActivity.EXTRA_CURRENCY);
        }

        if (extras.containsKey(PrepareOrderActivity.EXTRA_TAX)) {
            order.product.tax = extras.getParcelable(PrepareOrderActivity.EXTRA_TAX);
        }

        order.user = UserManager.get(this).getUser();
    }

    private void checkoutOrder(Order order) {
        prepareOrder(order);
        // TODO REMOVE THIS ONLY FOR TESTING
        order.address = order.user.addresses.get(0);

        if (order.user.paymentCards.size() > 0) {
            order.card = order.user.paymentCards.get(0);
        }

        Intent intent = new Intent(this, ProcessOrderActivity.class);
        intent.putExtra(ShopeliaActivity.EXTRA_ORDER, order);
        startActivityForResult(intent, ShopeliaActivity.REQUEST_CHECKOUT);
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
        new UserAPI(this, new CallbackAdapter() {

            @Override
            public void onSignIn(User user) {
                super.onSignIn(user);
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            switchFragmentsNow(inFragment, outFragment, false);
            return;
        }
        final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.shopelia_fade_out_short);
        final Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.shopelia_fade_in_short);
        fadeOut.setDuration(200);
        fadeIn.setDuration(200);
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
                final ExtendedFrameLayout container = (ExtendedFrameLayout) findViewById(R.id.fragment_container);
                container.lockDraw();
                switchFragmentsNow(inFragment, outFragment, true);
                final ResizeAnimation resize = new ResizeAnimation(container, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                resize.setDuration(150);
                resize.setInterpolator(new AccelerateDecelerateInterpolator());
                resize.computeSize(new OnViewRectComputedListener() {

                    @Override
                    public void onViewRectComputed(View victim, Rect from, Rect to) {
                        resize.setAnimationListener(new AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                container.startAnimation(fadeIn);
                                container.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                                container.unlockDraw();
                            }
                        });
                        container.startAnimation(resize);
                    }
                });
            }
        });
        findViewById(R.id.fragment_container).startAnimation(fadeOut);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.fragment_container).getWindowToken(), 0);
    }

    public void switchFragmentsNow(final ShopeliaFragment<?> inFragment, final ShopeliaFragment<?> outFragment, boolean forceExecute) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        try {
            mSavedStates.put(outFragment.getClass(), fm.saveFragmentInstanceState(outFragment));
        } catch (Exception e) {
            // Do nothing : We cannot retrieve the Fragment and this
            // causes an IllegalStateException. Unfortunately it
            // seems to be impossible to know if the fragment is or
            // not in the FragmentManager. The only solution seems
            // to catch the exception. Sad...
        }
        if (mSavedStates.containsKey(inFragment.getClass())) {
            inFragment.setInitialSavedState(mSavedStates.get(inFragment.getClass()));
        }
        ft.replace(R.id.fragment_container, inFragment, inFragment.getName());
        ft.commit();
        if (forceExecute) {
            fm.executePendingTransactions();
        }
    }

    @Override
    public int getSignInViewCount() {
        return mSignInViewCount;
    }

    @Override
    public void incSignInViewCount() {
        mSignInViewCount++;
    }
}
