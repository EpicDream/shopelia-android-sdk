package com.shopelia.android.api.widget;

/**
 * A view used by Shopelia in order to trigger ShopeliaSDK launch.
 * 
 * @author Pierre Pollastri
 */
public interface ShopeliaView {

    public void callCheckout();

    public void setProductUrl(CharSequence url);

    public String getProductUrl();

}
