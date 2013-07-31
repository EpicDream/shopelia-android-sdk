package com.shopelia.android;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.shopelia.android.AuthenticateFragment.OnUserAuthenticateListener;
import com.shopelia.android.WelcomeFragment.WelcomeParent;
import com.shopelia.android.api.Shopelia;
import com.shopelia.android.app.ShopeliaActivity;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Merchant;
import com.shopelia.android.model.User;

public class WelcomeActivity extends ShopeliaActivity implements WelcomeParent, OnUserAuthenticateListener {

    public static final String ACTIVITY_NAME = "Welcome Activity";

    public static final int REQUEST_SHOPELIA = 0x100;
    public static final int REQUEST_MERCHANT = 0x101;

    public static final long SHOW_WAITING_DIALOG_DELAY = 2000L;

    public boolean mIsCanceling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final UserManager um = UserManager.get(this);
        if (!um.isLogged() && um.getAccount() == null) {
            setActivityStyle(STYLE_TRANSLUCENT);
        }
        super.onCreate(savedInstanceState);
        if (!um.isLogged() && um.hasAccountPermission() && um.getAccount() != null) {
            setHostContentView(R.layout.shopelia_loading_layout);
            um.restoreFromAccount(new AccountManagerCallback<Bundle>() {

                @Override
                public void run(AccountManagerFuture<Bundle> result) {
                    try {
                        if (result.isDone()) {
                            Bundle data = result.getResult();
                            String authToken = data.getString(AccountManager.KEY_AUTHTOKEN);
                            String userString = data.getString(AccountManager.KEY_USERDATA);
                            User user = userString != null ? User.inflate(new JSONObject(userString)) : new User();
                            if (user.email == null) {
                                user.email = um.getAccount().name;
                                authToken = um.getAccount().name;
                            }
                            um.login(user, authToken);
                            um.setAutoSignIn(false);
                        }
                    } catch (OperationCanceledException e) {
                        e.printStackTrace();
                    } catch (AuthenticatorException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {

                    }
                    if (result.isDone()) {
                        init();
                    }
                }
            });
        } else {
            init();
        }
    }

    private void init() {
        UserManager um = UserManager.get(this);
        if (um.isLogged() && um.hasAccountPermission() && um.getAccount() == null) {
            um.logout();
            startActivityForResult(new Intent(this, PrepareOrderActivity.class), REQUEST_CHECKOUT);
            return;
        } else if (um.getUser() != null && !um.isAutoSignedIn()) {
            setHostContentView(R.layout.shopelia_welcome_activity);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, new AuthenticateFragment());
            ft.commit();
        } else if (um.getCheckoutCount() > 0 || um.isAutoSignedIn()) {
            startActivityForResult(new Intent(this, PrepareOrderActivity.class), 0);
        } else {
            setHostContentView(R.layout.shopelia_welcome_activity);
            getShopeliaActionBar().hide();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, new WelcomeFragment());
            ft.commit();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtras(getIntent().getExtras());
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MERCHANT) {
            resultCode = Shopelia.RESULT_REDIRECT_ON_MERCHANT;
        }
        setResult(resultCode, data);
        finish();
    }

    @Override
    public String getActivityName() {
        return ACTIVITY_NAME;
    }

    @Override
    protected boolean isPartOfOrderWorkFlow() {
        return false;
    }

    @Override
    public void continueWithShopelia() {
        startActivityForResult(new Intent(this, PrepareOrderActivity.class), REQUEST_CHECKOUT);
    }

    @Override
    public void continueWithMerchant() {
        String url = getIntent().getExtras().getString(Shopelia.EXTRA_PRODUCT_URL);
        super.startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(url)), REQUEST_MERCHANT);
    }

    @Override
    public Merchant getMerchant() {
        return getIntent().getExtras().getParcelable(Shopelia.EXTRA_MERCHANT);
    }

    @Override
    public void cancel() {
        onBackPressed();
    }

    @Override
    public void onUserAuthenticate(boolean autoSignIn) {
        UserManager.get(this).setAutoSignIn(autoSignIn);
        startActivityForResult(new Intent(this, PrepareOrderActivity.class), REQUEST_CHECKOUT);
    }

}
