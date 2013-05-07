package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class OrderState implements Parcelable {

    public static final float NO_PRICE = 0.f;

    public enum State {
        ERROR("error"), PENDING_CONFIRMATION("pending_confirmation"), ORDERING("ordering"), SUCCESS("success"), FINALIZING("finalizing");

        private String mLabel;

        State(String label) {
            mLabel = label;
        }

        public static State fromLabel(String label) {
            State[] states = values();
            for (State state : states) {
                if (state.mLabel.equals(label)) {
                    return state;
                }
            }
            return ORDERING;
        }

    }

    public enum Verb {
        CONFIRM("confirm"), CANCEL("cancel");

        private String mVerb;

        Verb(String verb) {
            mVerb = verb;
        }

        @Override
        public String toString() {
            return mVerb;
        }

    }

    public enum Message {
        LOGGED("logged"), EMPTY_CART("empty_cart"), NOTHING("");

        private String mLabel;

        Message(String label) {
            mLabel = label;
        }

        public static Message fromLabel(String label) {
            Message[] messages = values();
            for (Message message : messages) {
                if (message.mLabel.equals(label)) {
                    return message;
                }
            }
            return NOTHING;
        }

    }

    public interface Api {
        String ORDER = Order.Api.ORDER;

        String UUID = Order.Api.UUID;
        String URL = Order.Api.PRODUCT_URL;
        String STATE = "state";
        String MESSAGE = "message";
        String PRICE_PRODUCT = "price_total";
        String PRICE_DELIVERY = "price_delivery";
        String PRICE_TOTAL = "price_total";

        String PRODUCTS = "products";

        String CONTENT = "content";
        String VERB = "verb";

    }

    public String uuid = Order.NO_ID;
    public State state;
    public Message message;

    public float productPrice;
    public float deliveryPrice;
    public float totalPrice;

    public OrderState() {

    }

    private OrderState(Parcel source) {
        uuid = source.readString();
        state = State.fromLabel(source.readString());
        message = Message.fromLabel(source.readString());
        productPrice = source.readFloat();
        deliveryPrice = source.readFloat();
        totalPrice = source.readFloat();
    }

    public static OrderState inflate(JSONObject object) throws JSONException {
        Log.d(null, object.toString());
        OrderState state = new OrderState();
        state.uuid = object.getString(Api.UUID);
        state.state = State.fromLabel(object.getString(Api.STATE));
        state.message = Message.fromLabel(object.optString(Api.MESSAGE));
        state.productPrice = (float) object.optDouble(Api.PRICE_PRODUCT, NO_PRICE);
        state.deliveryPrice = (float) object.optDouble(Api.PRICE_DELIVERY, NO_PRICE);
        state.totalPrice = (float) object.optDouble(Api.PRICE_TOTAL, NO_PRICE);
        if (state.productPrice != NO_PRICE) {
            state.totalPrice = state.productPrice + state.deliveryPrice;
        }
        return state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(state.mLabel);
        dest.writeString(message.mLabel);
        dest.writeFloat(productPrice);
        dest.writeFloat(deliveryPrice);
        dest.writeFloat(totalPrice);
    }

    public static final Parcelable.Creator<OrderState> CREATOR = new Creator<OrderState>() {

        @Override
        public OrderState[] newArray(int size) {
            return new OrderState[size];
        }

        @Override
        public OrderState createFromParcel(Parcel source) {
            return new OrderState(source);
        }
    };

}
