package com.shopelia.android.analytics;

public interface Analytics {

    public interface Events {

        public interface UserInteractions {
            String FOCUS_IN = "In";
            String OK = "OK";
            String CLICK_ON = "Click On";

            public interface Fields {
                String EMAIL = "Email";
                String PHONE = "Phone";
                String NAME = "Full Name";
                String ADDRESS_1 = "Address 1";
                String ADDRESS_2 = "Address 2";
                String PASSWORD = "Password";
                String ZIP = "Zip";
                String COUNTRY = "Country";
                String CITY = "City";
                String CARD_NUMBER = "Card Number";
                String EXPIRY_DATE = "Expiry Date";
                String CVV = "CVV";
                String SCAN_CARD = "Scan Card";
            }

        }

        public interface Activities {
            String DISPLAY = "Display";
        }

        String IDENTIFY = "Identify";
        String UNIDENTIFY = "Unidentify";
    }

    public interface Properties {
        String SESSION = "Session ID";
        String SDK = "SDK";
        String SDK_VERSION = "SDK Version";

    }

}
