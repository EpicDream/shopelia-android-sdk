package com.shopelia.android;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Toast;

import com.shopelia.android.SignInFragment.OnSignInListener;
import com.shopelia.android.SignUpFragment.OnSignUpListener;
import com.shopelia.android.analytics.Analytics;
import com.shopelia.android.app.AccountAuthenticatorShopeliaActivity;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.app.ShopeliaFragment;
import com.shopelia.android.config.Config;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.Merchant;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.Product;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiHandler;
import com.shopelia.android.remote.api.ApiHandler.CallbackAdapter;
import com.shopelia.android.remote.api.ApiHandler.ErrorInflater;
import com.shopelia.android.remote.api.ProductAPI;
import com.shopelia.android.remote.api.UserAPI;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.Tax;
import com.shopelia.android.view.animation.ResizeAnimation;
import com.shopelia.android.view.animation.ResizeAnimation.OnViewRectComputedListener;
import com.shopelia.android.widget.ExtendedFrameLayout;
import com.shopelia.android.widget.FormListFooter;
import com.shopelia.android.widget.FormListHeader;
import com.shopelia.android.widget.ProductSheetWidget;
import com.shopelia.android.widget.ValidationButton;
import com.shopelia.android.widget.form.SingleLinePaymentCardField;
import com.turbomanage.httpclient.HttpResponse;

public class PrepareOrderActivity extends AccountAuthenticatorShopeliaActivity implements OnSignUpListener, OnSignInListener {

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
    private static final int REQUEST_ADD_PAYMENT_CARD = 0x0103;

    private SignInFragment mSignInFragment = new SignInFragment();
    private SignUpFragment mSignUpFragment = new SignUpFragment();
    private ScrollView mScrollView;

    private Product mProduct;

    private int mSignInViewCount = 0;

    private boolean mCardScanned = false;

    // Cache
    private String mCachedPincode = null;

    private Map<Class<?>, Fragment.SavedState> mSavedStates = new HashMap<Class<?>, Fragment.SavedState>();

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getIntent().putExtra(EXTRA_INIT_ORDER, true);
        setActivityStyle(STYLE_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setHostContentView(R.layout.shopelia_prepare_order_activity);
        mScrollView = (ScrollView) findViewById(R.id.scrollview);

        if (isCalledByAcountManager() && UserManager.get(this).getAccount() != null) {
            Toast.makeText(this, R.string.shopelia_account_only_one, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!isCalledByAcountManager() && (getIntent().getExtras() == null || !getIntent().getExtras().containsKey(EXTRA_PRODUCT_URL))) {
            finish();
            return;
        }

        if (savedInstanceState == null) {
            createSessionId(System.currentTimeMillis(), getIntent().getStringExtra(EXTRA_PRODUCT_URL));
            if (!UserManager.get(this).isLogged() || isCalledByAcountManager()) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                if (UserManager.get(this).getLoginsCount() > 0) {
                    ft.add(R.id.fragment_container, mSignInFragment, mSignInFragment.getName());
                } else {
                    ft.add(R.id.fragment_container, mSignUpFragment, mSignUpFragment.getName());
                }
                ft.commit();
            } else {
                checkoutOrder(getOrder());
            }
        }
        new FormListHeader(this).setView(findViewById(R.id.header));
        new FormListFooter(this).setView(findViewById(R.id.footer));
        if (isCalledByAcountManager()) {
            findViewById(R.id.header).setVisibility(View.GONE);
            ((LinearLayout) findViewById(R.id.main_form)).setGravity(Gravity.TOP);
        } else {
            Product product = Product.inflate(getIntent().getExtras());
            new ProductAPI(this, new CallbackAdapter() {
                @Override
                public void onProductUpdate(Product product, boolean fromNetwork) {
                    if (product.isValid()) {
                        mProduct = product;
                        ((ProductSheetWidget) findViewById(R.id.product_sheet)).setProductInfo(product, fromNetwork);
                    }
                }
            }).getProduct(product);
        }
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
                    mCachedPincode = order.user.pincode;
                    createAccount();
                }
                break;
            case REQUEST_ADD_PAYMENT_CARD:
                if (resultCode == RESULT_OK) {
                    getOrder().user.addPaymentCard((PaymentCard) data.getParcelableExtra(AddPaymentCardActivity.EXTRA_PAYMENT_CARD));
                    checkoutOrder(getOrder());
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
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
        getOrder().user.pincode = mCachedPincode;
        createAccount();
    }

    private void createAccount() {
        final Order order = getOrder();
        startWaiting(getString(R.string.shopelia_form_main_waiting), true, true);
        new UserAPI(this, new ApiHandler.CallbackAdapter() {

            @Override
            public void onAccountCreationSucceed(final User user, Address address) {
                super.onAccountCreationSucceed(user, address);
                if (mCardScanned) {
                    getTracker().track(Analytics.Events.AddPaymentCardMethod.CARD_SCANNED);
                } else {
                    getTracker().track(Analytics.Events.AddPaymentCardMethod.CARD_NOT_SCANNED);
                }
                stopWaiting();
                order.user = user;
                if (isCalledByAcountManager()) {
                    finishAccountRegistration(user);
                } else {
                    if (user.paymentCards.size() == 0) {
                        Intent intent = new Intent(PrepareOrderActivity.this, AddPaymentCardActivity.class);
                        intent.putExtra(AddPaymentCardActivity.EXTRA_REQUIRED, true);
                        startActivityForResult(intent, REQUEST_ADD_PAYMENT_CARD);
                    } else {
                        checkoutOrder(order);
                    }
                }
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

    private void finishAccountRegistration(User user) {
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, user.email);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Config.ACCOUNT_TYPE);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void prepareOrder(Order order) {
        order.product = mProduct != null ? mProduct : Product.inflate(getIntent().getExtras());

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
        order.address = order.user.getDefaultAddress();

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
                stopWaiting();
                super.onSignIn(user);
                if (isCalledByAcountManager()) {
                    finishAccountRegistration(user);
                } else {
                    Order order = getOrder();
                    order.user = user;
                    checkoutOrder(order);
                }
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
