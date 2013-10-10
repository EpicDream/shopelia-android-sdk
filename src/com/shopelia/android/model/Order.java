package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

import com.shopelia.android.utils.ParcelUtils;

public class Order implements BaseModel<Order> {

    public interface Api {
        String UUID = "uuid";
        String USER = "user";
        String ADDRESS = "address";
        String PAYMENT_CARD = "payment_card";
        String PRODUCT = "product";
        String PRODUCTS = "products";
        String EXPECTED_PRICE_TOTAL = "expected_price_total";
        String EXPECTED_CASHFRONT_VALUE = "expected_cashfront_value";
        String EXPECTED_SHIPPING_PRICE = "expected_shipping_price";
        String EXPECTED_PRODUCT_PRICE = "expected_product_price";
        String ORDER = "order";
    }

    public String uuid = NO_UUID;

    public Product product;

    // Shipping
    public Address address;

    // Payment card
    public PaymentCard card;

    // User
    public User user;

    public Order() {

    }

    private Order(Parcel source) {
        uuid = source.readString();
        product = ParcelUtils.readParcelable(source, Product.class.getClassLoader());
        card = ParcelUtils.readParcelable(source, PaymentCard.class.getClassLoader());
        user = ParcelUtils.readParcelable(source, User.class.getClassLoader());
        address = ParcelUtils.readParcelable(source, Address.class.getClassLoader());
    }

    public void updateUser(User update) {
        if (!update.addresses.contains(address)) {
            address = update.getDefaultAddress();
        }
        if (!update.paymentCards.contains(card)) {
            card = update.getDefaultPaymentCard();
        }
        user = update;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        ParcelUtils.writeParcelable(dest, product, flags);
        ParcelUtils.writeParcelable(dest, card, flags);
        ParcelUtils.writeParcelable(dest, user, flags);
        ParcelUtils.writeParcelable(dest, address, flags);
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
        order.uuid = object.optString(Api.UUID, NO_UUID);
        try {
            order.product = Product.inflate(object.getJSONObject(Api.PRODUCT));
        } catch (JSONException e) {
            // Nothing to be done
        }
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

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(Api.UUID, uuid);
        object.put(Api.ADDRESS, address != null ? address.toJson() : null);
        object.put(Api.USER, user != null ? user.toJson() : null);
        object.put(Api.PRODUCT, product != null ? product.toJson() : null);
        object.put(Api.PAYMENT_CARD, card != null ? card.toJson() : null);
        return object;
    }

    @Override
    public void merge(Order item) {
        // TODO Auto-generated method stub

    }

    @Override
    public long getId() {
        return uuid != NO_UUID ? uuid.hashCode() : NO_ID;
    }

    @Override
    public boolean isValid() {
        // TODO Auto-generated method stub
        return false;
    }

}
