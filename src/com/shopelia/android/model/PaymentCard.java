package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class PaymentCard implements JsonData, Parcelable {

    public interface Api {
        String PAYMENT_CARD = "payment_card";
        String PAYMENT_CARDS = "payment_cards";

        String ID = "id";
        String NUMBER = "number";
        String EXP_MONTH = "exp_month";
        String EXP_YEAR = "exp_year";
        String CVV = "cvv";
    }

    private static final long INVALID_ID = -1;

    public long id = INVALID_ID;
    public String number;
    public String expMonth;
    public String expYear;
    public String cvv;

    public PaymentCard() {

    }

    public PaymentCard(JSONObject json) throws JSONException {
        id = json.optLong(Api.ID, INVALID_ID);
        number = json.getString(Api.NUMBER);
        expMonth = json.getString(Api.EXP_MONTH);
        expYear = json.getString(Api.EXP_YEAR);
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
        json.put(Api.EXP_YEAR, expYear);
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

}
