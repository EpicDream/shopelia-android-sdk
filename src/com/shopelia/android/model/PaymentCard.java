package com.shopelia.android.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class PaymentCard implements BaseModel<PaymentCard> {

    public interface Api {
        String PAYMENT_CARD = "payment_card";
        String PAYMENT_CARDS = "payment_cards";

        String ID = "id";
        String NUMBER = "number";
        String EXP_MONTH = "exp_month";
        String EXP_YEAR = "exp_year";
        String CVV = "cvv";

        String NAME = "lastname";

        String PAYMENT_CARD_ID = "payment_card_id";

        String EXPIRY_DATE = "expiry_date";

    }

    public static final String IDENTIFIER = PaymentCard.class.getName();

    public static final long INVALID_ID = -1;

    public long id = INVALID_ID;
    public String number;
    public String expMonth;
    public String expYear;
    public String cvv;

    public PaymentCard() {

    }

    private PaymentCard(Parcel source) {
        id = source.readLong();
        number = source.readString();
        expYear = source.readString();
        expMonth = source.readString();
        cvv = source.readString();
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (id != INVALID_ID) {
            json.put(Api.ID, id);
        }
        json.put(Api.NUMBER, number);
        json.put(Api.EXP_MONTH, expMonth);
        json.put(Api.EXP_YEAR, "20" + expYear);
        json.put(Api.CVV, cvv);
        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(number);
        dest.writeString(expYear);
        dest.writeString(expMonth);
        dest.writeString(cvv);
    }

    public static final Parcelable.Creator<PaymentCard> CREATOR = new Creator<PaymentCard>() {

        @Override
        public PaymentCard[] newArray(int size) {
            return new PaymentCard[size];
        }

        @Override
        public PaymentCard createFromParcel(Parcel source) {
            return new PaymentCard(source);
        }
    };

    public static PaymentCard inflate(JSONObject object) throws JSONException {
        PaymentCard card = new PaymentCard();

        card.id = object.optLong(Api.ID, INVALID_ID);
        card.number = object.getString(Api.NUMBER);
        card.expMonth = object.getString(Api.EXP_MONTH);
        card.expYear = object.getString(Api.EXP_YEAR);
        card.cvv = object.optString(Api.CVV);
        if (card.expYear != null && card.expYear.length() > 2) {
            card.expYear = card.expYear.substring(card.expYear.length() - 2);
        }
        return card;
    }

    public static ArrayList<PaymentCard> inflate(JSONArray array) throws JSONException {
        ArrayList<PaymentCard> cards = new ArrayList<PaymentCard>(array.length());
        final int size = array.length();
        for (int index = 0; index < size; index++) {
            cards.add(PaymentCard.inflate(array.getJSONObject(index)));
        }
        return cards;
    }

    @Override
    public void merge(PaymentCard item) {
        // TODO Auto-generated method stub

    }

}
