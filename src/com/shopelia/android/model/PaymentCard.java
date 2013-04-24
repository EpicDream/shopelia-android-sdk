package com.shopelia.android.model;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentCard implements JsonData {

    public interface Api {
        String PAYMENT_CARD = "payment_card";
        String PAYMENT_CARDS = "payment_cards";

        String ID = "id";
        String NUMBER = "number";
        String EXP_MONTH = "exp_month";
        String EXP_YEAR = "exp_year";
        String CVV = "cvv";
    }

    public long id;
    public String number;
    public String expMonth;
    public String expYear;
    public String cvv;

    public PaymentCard(JSONObject json) throws JSONException {
        id = json.getLong(Api.ID);
        number = json.getString(Api.NUMBER);
        expMonth = json.getString(Api.EXP_MONTH);
        expYear = json.getString(Api.EXP_YEAR);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Api.ID, id);
        json.put(Api.NUMBER, number);
        json.put(Api.EXP_MONTH, expMonth);
        json.put(Api.EXP_YEAR, expYear);
        json.put(Api.CVV, cvv);
        return json;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Api.ID, id);
        json.put(Api.NUMBER, number);
        json.put(Api.EXP_MONTH, expMonth);
        json.put(Api.EXP_YEAR, expYear);
        json.put(Api.CVV, cvv);
        return json;
    }

}
