package com.shopelia.android.test.model;

import com.shopelia.android.model.Address;

public class MockAddress extends Address {

    public static MockAddress get(String name) {
        return MockModelHelper.get(new MockAddress(), name);
    }

    public void test() {
        address = "12 rue du test sur Android";
        city = "Paris";
        country = "FR";
        extras = "3ème escalier à gauche";
        firstname = "Test";
        lastname = "Shopelia";
        phone = "06058645869";
        zipcode = "75001";

    }
}
