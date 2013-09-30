package com.shopelia.android.api.widget;

import com.shopelia.android.api.Shopelia.OnProductAvailabilityChangeListener;

/**
 * A view used by Shopelia in order to trigger ShopeliaSDK launch.
 * 
 * @author Pierre Pollastri
 */
public interface ShopeliaView {

    public void callCheckout();

    public void setProductUrl(CharSequence url);

    public String getProductUrl();

    public boolean canCheckout();

    public void setOnProductAvailabilityChangeListener(OnProductAvailabilityChangeListener l);

    public void setTrackerName(String name);

}
