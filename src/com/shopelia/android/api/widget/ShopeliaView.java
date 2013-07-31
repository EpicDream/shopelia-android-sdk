package com.shopelia.android.api.widget;

import android.net.Uri;

/**
 * A view used by Shopelia in order to trigger ShopeliaSDK launch.
 * 
 * @author Pierre Pollastri
 */
public interface ShopeliaView {

    public interface OnProductAvailabilityChangeListener {
        public void onProductAvailabilityChange(String url, boolean available);
    }

    public void callCheckout();

    public void setProductUrl(CharSequence url);

    public String getProductUrl();

    public boolean canCheckout();

    public void setProductName(String name);

    public void setProductPrice(float price);

    public void setProductDeliveryPrice(float shippingPrice);

    public void setProductImage(Uri imageUri);

    public void setProductShippingExtras(String shippingExtras);

    public void setOnProductAvailabilityChangeListener(OnProductAvailabilityChangeListener l);

}
