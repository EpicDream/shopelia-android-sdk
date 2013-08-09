package com.shopelia.android.test.model;

import android.net.Uri;

import com.shopelia.android.model.Merchant;

public class MockMerchant extends Merchant {

    public static MockMerchant get(String name) {
        return MockModelHelper.get(new MockMerchant(), name);
    }

    public void first() {
        logo = "";
        name = "TestMerchantFirst";
        uri = Uri.parse("http://testmerchant.com");
    }

}
