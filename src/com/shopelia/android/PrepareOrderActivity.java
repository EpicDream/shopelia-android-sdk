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
import com.shopelia.android.model.Order;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.Product;
import com.shopelia.android.model.User;
import com.shopelia.android.remote.api.ApiController.ErrorInflater;
import com.shopelia.android.remote.api.ApiController.OnApiErrorEvent;
import com.shopelia.android.remote.api.ProductAPI;
import com.shopelia.android.remote.api.ProductAPI.OnProductNotAvailable;
import com.shopelia.android.remote.api.ProductAPI.OnProductUpdateEvent;
import com.shopelia.android.remote.api.UserAPI;
import com.shopelia.android.remote.api.UserAPI.OnAccountCreationSucceedEvent;
import com.shopelia.android.remote.api.UserAPI.OnSignInEvent;
import com.shopelia.android.view.animation.ResizeAnimation;
import com.shopelia.android.view.animation.ResizeAnimation.OnViewRectComputedListener;
import com.shopelia.android.widget.ExtendedFrameLayout;
import com.shopelia.android.widget.FormListFooter;
import com.shopelia.android.widget.FormListHeader;
import com.shopelia.android.widget.ProductSheetWidget;
import com.shopelia.android.widget.ValidationButton;
import com.shopelia.android.widget.form.SingleLinePaymentCardField;

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

    // Cache
    private String mCachedPincode = null;

    private Map<Class<?>, Fragment.SavedState> mSavedStates = new HashMap<Class<?>, Fragment.SavedState>();

    private ProductAPI mProductAPI;

    private boolean mCardScanned = false;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getIntent().putExtra(EXTRA_INIT_ORDER, true);
        setActivityStyle(STYLE_FULLSCREEN);
        super.onCreate(savedInstanceState);
        mProductAPI = new ProductAPI(this);
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
            Product product = new Product(getIntent().getExtras().getString(EXTRA_PRODUCT_URL));
            mProductAPI.getProduct(product);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProductAPI.registerSticky(this);
    }

    public void onEventMainThread(OnProductUpdateEvent event) {
        if (event.resource.isValid()) {
            mProduct = event.resource;
            ((ProductSheetWidget) findViewById(R.id.product_sheet)).setProductInfo(event.resource, event.isFromNetwork);
        }
    }

    public void onEventMainThread(OnProductNotAvailable event) {
        ProductNotFoundFragment fragment = ProductNotFoundFragment.newInstance(event.resource);
        fragment.show(getSupportFragmentManager(), null);
    }

    public void onEventMainThread(OnApiErrorEvent event) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        mProductAPI.unregister(this);
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
        final UserAPI api = new UserAPI(this);
        api.register(new Object() {
            public void onEventMainThread(OnAccountCreationSucceedEvent event) {
                api.unregister(this);
                if (mCardScanned) {
                    getTracker().track(Analytics.Events.AddPaymentCardMethod.CARD_SCANNED);
                } else {
                    getTracker().track(Analytics.Events.AddPaymentCardMethod.CARD_NOT_SCANNED);
                }
                stopWaiting();
                order.user = event.resource;
                if (isCalledByAcountManager()) {
                    finishAccountRegistration(order.user);
                } else {
                    if (order.user.paymentCards.size() == 0) {
                        Intent intent = new Intent(PrepareOrderActivity.this, AddPaymentCardActivity.class);
                        intent.putExtra(AddPaymentCardActivity.EXTRA_REQUIRED, true);
                        startActivityForResult(intent, REQUEST_ADD_PAYMENT_CARD);
                    } else {
                        checkoutOrder(order);
                    }
                }
            }

            @SuppressWarnings("unused")
            public void onEventMainThread(OnApiErrorEvent event) {
                api.unregister(this);
                stopWaiting();
                if (event.exception != null) {
                    Toast.makeText(PrepareOrderActivity.this, R.string.shopelia_error_network_error, Toast.LENGTH_LONG).show();
                } else {
                    mSignUpFragment.onCreateAccountError(event.json);
                }
            }

        });
        api.createAccount(order.user, order.address, order.card);
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
        order.product = mProduct;

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
        final UserAPI api = new UserAPI(this);
        api.register(new Object() {
            @SuppressWarnings("unused")
            public void onEventMainThread(OnSignInEvent event) {
                api.unregister(this);
                stopWaiting();
                if (isCalledByAcountManager()) {
                    finishAccountRegistration(event.resource);
                } else {
                    Order order = getOrder();
                    order.user = event.resource;
                    checkoutOrder(order);
                }
            }

            @SuppressWarnings("unused")
            public void onEventMainThread(OnApiErrorEvent event) {
                api.unregister(this);
                stopWaiting();
                if (event.exception != null) {
                    if (Config.INFO_LOGS_ENABLED) {
                        event.exception.printStackTrace();
                    }
                    Toast.makeText(PrepareOrderActivity.this, R.string.shopelia_error_network_error, Toast.LENGTH_SHORT).show();
                }
                if (event.json != null) {
                    Toast.makeText(PrepareOrderActivity.this, event.json.optString(ErrorInflater.Api.ERROR), Toast.LENGTH_SHORT).show();
                }
            }

        });
        api.signIn(user);
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
