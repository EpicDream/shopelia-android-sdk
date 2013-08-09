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

}
