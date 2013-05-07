package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Order implements Parcelable {

    public interface Api {
        String UUID = "uuid";
        String USER = "user";
        String ADDRESS = "address";
        String PAYMENT_CARD = "payment_card";
        String PRODUCT_URL = "url";
        String PRODUCT_URLS = "urls";

        String ORDER = "order";

    }

    public static final String NO_ID = null;

    public String uuid = NO_ID;

    public Product product;

    // Shipping
    public Address address;

    // Payment card
    public PaymentCard card;

    // User
    public User user;

    // Current state
    public OrderState state;

    public Order() {

    }

    private Order(Parcel source) {
        uuid = source.readString();
        address = source.readParcelable(Address.class.getClassLoader());
        card = source.readParcelable(PaymentCard.class.getClassLoader());
        user = source.readParcelable(User.class.getClassLoader());
        state = source.readParcelable(OrderState.class.getClassLoader());
        product = source.readParcelable(Product.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeParcelable(address, flags);
        dest.writeParcelable(card, flags);
        dest.writeParcelable(user, flags);
        dest.writeParcelable(state, flags);
        dest.writeParcelable(product, flags);
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }

        @Override
        public Order createFromParcel(Parcel source) {
            return new Order(source);
        }
    };

    public static Order inflate(JSONObject object) {
        Order order = new Order();
        order.uuid = object.optString(Api.UUID, NO_ID);
        order.product = new Product();
        order.product.url = object.optString(Api.PRODUCT_URL);
        if (object.has(Api.ADDRESS)) {
            try {
                order.address = Address.inflate(object.getJSONObject(Api.ADDRESS));
            } catch (JSONException e) {
                // Nothing to be done
            }
        }
        if (object.has(Api.PAYMENT_CARD)) {
            try {
                order.card = PaymentCard.inflate(object.getJSONObject(Api.PAYMENT_CARD));
            } catch (JSONException e) {
                // Nothing to be done
            }
        }
        if (object.has(Api.USER)) {
            try {
                order.user = User.inflate(object.getJSONObject(Api.USER));
            } catch (JSONException e) {
                // Nothing to be done
            }
        }
        return order;
    }
}
