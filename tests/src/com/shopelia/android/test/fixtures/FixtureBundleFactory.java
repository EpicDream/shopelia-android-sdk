package com.shopelia.android.test.fixtures;

import android.os.Bundle;

import com.shopelia.android.api.Shopelia;
import com.shopelia.android.model.Product;
import com.shopelia.android.test.model.MockProduct;

public class FixtureBundleFactory {

    public static Bundle getShopeliaIntentBundle(String name) {
        Bundle out = new Bundle();
        Product product = MockProduct.get(name);
        out.putParcelable(Shopelia.EXTRA_MERCHANT, product.merchant);
        out.putString(Shopelia.EXTRA_PRODUCT_URL, product.url);
        out.putFloat(Shopelia.EXTRA_PRICE, product.productPrice);
        out.putFloat(Shopelia.EXTRA_SHIPPING_PRICE, product.deliveryPrice);
        out.putString(Shopelia.EXTRA_SHIPPING_INFO, product.shippingExtra);
        out.putString(Shopelia.EXTRA_PRODUCT_TITLE, product.name);
        return out;
    }
}
