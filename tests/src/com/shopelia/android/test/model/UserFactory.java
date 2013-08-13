package com.shopelia.android.test.model;

import com.shopelia.android.model.User;

public class UserFactory extends User {

    public static UserFactory get(String name) {
        return ModelFactoryHelper.get(new UserFactory(), name);
    }

    public void me() {
        email = "pierre.pollastri@prixing.fr";
        password = "teston";
    }

    public void test() {
        email = "test@shopelia.fr";
        password = "shopelia";
        addresses.add(AddressFactory.get("test"));
        paymentCards.add(PaymentCardFactory.get("test"));
        firstname = "Test";
        lastname = "Shopelia";
        has_password = 0;
    }

}
