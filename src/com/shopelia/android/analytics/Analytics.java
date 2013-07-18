package com.shopelia.android.analytics;

public interface Analytics {

    public interface Events {

        public interface Steps {

            public String ORDER_COMPLETED = "Order Completed";

        }

        public interface UserInteractions {
            String FOCUS_IN = "In";
            String OK = "OK";

            public interface Fields {
                String EMAIL = "Email";
                String PHONE = "Phone";
                String NAME = "Full Name";
                String ADDRESS_1 = "Address 1";
                String ADDRESS_2 = "Address 2";
                String ZIP = "Zip";
                String COUNTRY = "Country";
                String CITY = "City";
                String CARD_NUMBER = "Card Number";
                String EXPIRY_DATE = "Expiry Date";
                String CVV = "CVV";
                String SCAN_CARD = "Scan Card";
            }

        }

        public interface AddPaymentCardMethod {
            String CARD_SCANNED = "Payment Card Scanned";
            String CARD_NOT_SCANNED = "Payment Card Not Scanned";
        }

        public interface Activities {
            String DISPLAY = "Display";
        }

        public interface UInterface {
            String SHOPELIA_BUTTON_SHOWN = "Shopelia Button Shown";
            String SHOPELIA_BUTTON_CLICKED = "Shopelia Button Clicked";

        }

        public String ADD_ADDRESS_METHOD = "AddAddressMethod";

    }

    public interface Properties {
        String SESSION = "Session ID";
        String SDK = "SDK";
        String SDK_VERSION = "SDK_VERSION";

        public interface AddAddressMethod {
            String PLACES_AUTOCOMPLETE = "Places Autocompletion";
            String REVERSE_DIRECTORY = "Reverse Directory";
            String MANUAL = "Manual";
        }

    }

}
