package com.shopelia.android.test.model;

import com.shopelia.android.model.User;

public class MockUser extends User {

    public static MockUser get(String name) {
        return MockModelHelper.get(new MockUser(), name);
    }

    public void me() {
        email = "pierre.pollastri@prixing.fr";
        password = "teston";
    }

    public void test() {
        email = "test@shopelia.fr";
        password = "shopelia";
        addresses.add(MockAddress.get("test"));
        paymentCards.add(MockPaymentCard.get("test"));
        firstname = "Test";
        lastname = "Shopelia";
        has_password = 0;
    }

}
