package com.shopelia.android.test.model;

import java.util.Calendar;

import com.shopelia.android.model.PaymentCard;

public class MockPaymentCard extends PaymentCard {

    public static MockPaymentCard get(String name) {
        return MockModelHelper.get(new MockPaymentCard(), name);
    }

    public void test() {
        number = "4111111111111111";
        expMonth = "12";
        expYear = ("" + (Calendar.getInstance().get(Calendar.YEAR) + 1)).substring(2);
        cvv = "123";
    }

}
