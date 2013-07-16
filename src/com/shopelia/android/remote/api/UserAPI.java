package com.shopelia.android.remote.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shopelia.android.http.JsonAsyncCallback;
import com.shopelia.android.manager.UserManager;
import com.shopelia.android.model.Address;
import com.shopelia.android.model.Order;
import com.shopelia.android.model.PaymentCard;
import com.shopelia.android.model.User;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

public class UserAPI extends ApiHandler {

    public UserAPI(Context context, Callback callback) {
        super(context, callback);
    }

    public void createAccount(User user, Address address, PaymentCard card) {

        setCurrentStep(STEP_ACCOUNT_CREATION);
        JSONObject params = new JSONObject();

        try {
            params.put(Order.Api.USER, User.createObjectForAccountCreation(user, address, card));
        } catch (JSONException e) {
            fireError(STEP_ACCOUNT_CREATION, null, null, e);
            return;
        }
        UserManager.get(getContext()).logout();
        ShopeliaRestClient.V1(getContext()).post(Command.V1.Users.$, params, new JsonAsyncCallback() {

            @Override
            public void onComplete(HttpResponse response, JSONObject object) {
                if (hasCallback() && object.has(User.Api.USER) && object.has(User.Api.AUTH_TOKEN)) {
                    User user = User.inflate(object.optJSONObject(User.Api.USER));
                    UserManager.get(getContext()).login(user, object.optString(User.Api.AUTH_TOKEN));
                    UserManager.get(getContext()).saveUser();
                    if (user.addresses.size() > 0) {
                        getCallback().onAccountCreationSucceed(user, user.addresses.get(0));
                    } else {
                        fireError(STEP_ACCOUNT_CREATION, response, null, new IllegalStateException("No address registered"));
                    }
                } else {
                    fireError(STEP_ACCOUNT_CREATION, response, object, null);
                }
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                fireError(STEP_ACCOUNT_CREATION, null, null, e);
            }

        });

    }

    public void destroyUser(final long id) {
        if (id == User.NO_ID) {
            throw new IllegalAccessError("Cannot retrieve invalid user");
        }
        ShopeliaRestClient.V1(getContext()).authenticate(getContext());
        ShopeliaRestClient.V1(getContext()).delete(Command.V1.Users.User(id), null, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse response) {
                if (hasCallback()) {
                    getCallback().onUserDestroyed(id);
                }
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                fireError(STEP_RETRIEVE_USER, null, null, e);
            }
        });
    }

    public void updateUser() {
        ShopeliaRestClient.V1(getContext()).get(Command.V1.Users.User(UserManager.get(getContext()).getUser().id), null,
                new AsyncCallback() {

                    @Override
                    public void onError(Exception e) {
                        super.onError(e);
                        fireError(STEP_RETRIEVE_USER, null, null, e);
                        if (hasCallback()) {
                            getCallback().onUserUpdateDone();
                        }
                    }

                    @Override
                    public void onComplete(HttpResponse httpResponse) {
                        if (httpResponse.getStatus() == 401 && hasCallback()) {
                            getCallback().onAuthTokenRevoked();
                        } else {
                            try {
                                User user = User.inflate(new JSONObject(httpResponse.getBodyAsString()).getJSONObject(User.Api.USER));
                                UserManager.get(getContext()).update(user);
                                if (hasCallback()) {
                                    getCallback().onUserRetrieved(user);
                                    getCallback().onUserUpdateDone();
                                }
                            } catch (JSONException e) {
                                onError(e);
                            }
                        }
                    }

                });
    }

    public void signIn(User user) {
        setCurrentStep(STEP_SIGN_IN);
        JSONObject params = new JSONObject();
        try {
            params.put(User.Api.EMAIL, user.email);
            params.put(User.Api.PASSWORD, user.password);
        } catch (JSONException e) {
            fireError(STEP_SIGN_IN, null, null, e);
            return;
        }

        ShopeliaRestClient.V1(getContext()).post(Command.V1.Users.SignIn.$, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {
                JSONObject object;
                try {
                    object = new JSONObject(httpResponse.getBodyAsString());
                } catch (JSONException e) {
                    fireError(STEP_SIGN_IN, httpResponse, null, e);
                    return;
                }
                if (httpResponse.getStatus() == 200) {
                    User user = User.inflate(object.optJSONObject(User.Api.USER));
                    UserManager.get(getContext()).login(user, object.optString(User.Api.AUTH_TOKEN));
                    UserManager.get(getContext()).saveUser();
                    getCallback().onSignIn(user);
                } else {
                    fireError(STEP_SIGN_IN, httpResponse, ErrorInflater.inflate(httpResponse.getBodyAsString()), null);
                }
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                fireError(STEP_SIGN_IN, null, null, e);
            }

        });
    }

    public void signOut(final String email) {
        ParameterMap params = ShopeliaRestClient.V1(getContext()).newParams();
        params.add(User.Api.EMAIL, email);
        ShopeliaRestClient.V1(getContext()).authenticate(getContext());
        ShopeliaRestClient.V1(getContext()).delete(Command.V1.Users.SignOut(), params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {
                if (hasCallback()) {
                    getCallback().onSignOut();
                }
            }
        });

    }
}
