package com.shopelia.android.test.model;

import com.shopelia.android.model.Product;
import com.shopelia.android.utils.Currency;
import com.shopelia.android.utils.Tax;

public class ProductFactory extends Product {

    public static ProductFactory get(String name) {
        return ModelFactoryHelper.get(new ProductFactory(), name);
    }

    public void first() {
        currency = Currency.EUR;
        productPrice = 12.59f;
        deliveryPrice = 2.79f;
        name = "Super product";
        description = "A super product";
        url = MerchantFactory.get("first").uri.toString() + "/super_product";
        merchant = MerchantFactory.get("first");
        shippingExtra = "Very fast";
        tax = Tax.ATI;
    }

}
