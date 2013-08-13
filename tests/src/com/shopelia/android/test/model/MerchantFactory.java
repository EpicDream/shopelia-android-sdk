package com.shopelia.android.test.model;

import android.net.Uri;

import com.shopelia.android.model.Merchant;

public class MerchantFactory extends Merchant {

    public static MerchantFactory get(String name) {
        return ModelFactoryHelper.get(new MerchantFactory(), name);
    }

    public void first() {
        logo = "";
        name = "TestMerchantFirst";
        uri = Uri.parse("http://testmerchant.com");
    }

}
