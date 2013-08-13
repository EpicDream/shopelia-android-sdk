package com.shopelia.android.test.model;

import java.util.Calendar;

import com.shopelia.android.model.PaymentCard;

public class PaymentCardFactory extends PaymentCard {

    public static PaymentCardFactory get(String name) {
        return ModelFactoryHelper.get(new PaymentCardFactory(), name);
    }

    public void test() {
        number = "4111111111111111";
        expMonth = "12";
        expYear = ("" + (Calendar.getInstance().get(Calendar.YEAR) + 1)).substring(2);
        cvv = "123";
    }

}
