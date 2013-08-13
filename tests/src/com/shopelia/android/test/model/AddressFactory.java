package com.shopelia.android.test.model;

import com.shopelia.android.model.Address;

public class AddressFactory extends Address {

    public static AddressFactory get(String name) {
        return ModelFactoryHelper.get(new AddressFactory(), name);
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

    public void second() {
        address = "12 rue du second test sur Android";
        city = "Paris";
        country = "FR";
        extras = "3ème escalier à gauche";
        firstname = "Test";
        lastname = "Shopelia";
        phone = "06058645869";
        zipcode = "75001";

    }
}
