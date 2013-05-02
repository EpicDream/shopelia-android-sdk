package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class OrderState {

    public enum State {
        ERROR("error"), PENDING_CONFIRMATION("pending_confirmation"), ORDERING(""), SUCCESS("success");

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
        String PRICE_PRODUCT = "price_product";
        String PRICE_DELIVERY = "price_delivery";
        String PRICE_TOTAL = "price_total";

        String CONTENT = "content";
        String VERB = "content";

    }

    public String uuid = Order.NO_ID;
    public State state;
    public Message message;

    public OrderState() {

    }

    public static OrderState inflate(JSONObject object) throws JSONException {
        Log.d(null, object.toString());
        OrderState state = new OrderState();
        state.uuid = object.getString(Api.UUID);
        state.state = State.fromLabel(object.getString(Api.STATE));
        state.message = Message.fromLabel(object.optString(Api.MESSAGE));
        return state;
    }

}
