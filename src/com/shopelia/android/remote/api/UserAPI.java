package com.shopelia.android.remote.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

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
            Log.d(null, "SEND " + params.toString(2));
        } catch (JSONException e) {
            fireError(STEP_ACCOUNT_CREATION, null, null, e);
            return;
        }
        ShopeliaRestClient.reset();
        UserManager.get(getContext()).logout();
        ShopeliaRestClient.post(Command.V1.Users.$, params, new JsonAsyncCallback() {

            @Override
            public void onComplete(HttpResponse response, JSONObject object) {
                if (hasCallback() && object.has(User.Api.USER) && object.has(User.Api.AUTH_TOKEN)) {
                    User user = User.inflate(object.optJSONObject(User.Api.USER));
                    UserManager.get(getContext()).login(user);
                    UserManager.get(getContext()).setAuthToken(object.optString(User.Api.AUTH_TOKEN));
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
        ShopeliaRestClient.authenticate(getContext());
        ShopeliaRestClient.delete(Command.V1.Users.User(id), null, new AsyncCallback() {

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

    public void retrieveUser(long id) {
        if (id == User.NO_ID) {
            throw new IllegalAccessError("Cannot retrieve invalid user");
        }
        ShopeliaRestClient.authenticate(getContext());
        ShopeliaRestClient.get(Command.V1.Users.User(id), null, new JsonAsyncCallback() {

            @Override
            public void onComplete(HttpResponse response, JSONObject object) {
                try {
                    User user = User.inflate(object.getJSONObject(User.Api.USER));
                    UserManager.get(getContext()).login(user);
                    if (hasCallback()) {
                        getCallback().onUserRetrieved(user);
                    }
                } catch (JSONException e) {
                    onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                fireError(STEP_RETRIEVE_USER, null, null, e);
            }

        });
    }

    public void sendPaymentInformation(final User user, PaymentCard card) {
        JSONObject params = new JSONObject();
        setCurrentStep(STEP_SEND_PAYMENT_INFORMATION);
        try {
            JSONObject cardObject = card.toJson();
            cardObject.put(PaymentCard.Api.NAME, user.lastName);
            params.put(PaymentCard.Api.PAYMENT_CARD, cardObject);

        } catch (JSONException e) {
            fireError(STEP_SEND_PAYMENT_INFORMATION, null, null, e);
            return;
        }
        ShopeliaRestClient.authenticate(getContext());
        ShopeliaRestClient.post(Command.V1.PaymentCards.$, params, new JsonAsyncCallback() {

            @Override
            public void onComplete(HttpResponse response, JSONObject object) {
                try {
                    PaymentCard card = PaymentCard.inflate(object.getJSONObject(PaymentCard.Api.PAYMENT_CARD));
                    user.paymentCards.add(card);
                    if (hasCallback()) {
                        getCallback().onPaymentInformationSent(card);
                    }
                } catch (JSONException e) {
                    fireError(STEP_SEND_PAYMENT_INFORMATION, response, null, e);
                }
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                fireError(STEP_SEND_PAYMENT_INFORMATION, null, null, e);
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

        ShopeliaRestClient.post(Command.V1.Users.SignIn.$, params, new AsyncCallback() {

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
                    UserManager.get(getContext()).login(user);
                    UserManager.get(getContext()).setAuthToken(object.optString(User.Api.AUTH_TOKEN));
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
        ParameterMap params = ShopeliaRestClient.newParams();
        params.add(User.Api.EMAIL, email);
        ShopeliaRestClient.authenticate(getContext());
        ShopeliaRestClient.delete(Command.V1.Users.SignOut(), params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {
                if (hasCallback()) {
                    getCallback().onSignOut();
                }
            }
        });

    }
}
