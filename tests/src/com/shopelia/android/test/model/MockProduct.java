package com.shopelia.android.test.model;

import com.shopelia.android.model.Product;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.Tax;

public class MockProduct extends Product {

    public static MockProduct get(String name) {
        return MockModelHelper.get(new MockProduct(), name);
    }

    public void first() {
        currency = Currency.EUR;
        productPrice = 12.59f;
        deliveryPrice = 2.79f;
        name = "Super product";
        description = "A super product";
        url = MockMerchant.get("first").uri.toString() + "/super_product";
        merchant = MockMerchant.get("first");
        shippingExtra = "Very fast";
        tax = Tax.ATI;
    }

}
