package com.shopelia.android.analytics;

public interface Analytics {

    public interface Events {

        public interface Steps {

            public interface SignUp {
                String BEGIN = "Begin Sign Up";
                String SIGNING_UP = "Signing Up";
                String SIGN_UP_ACTION = "Sign Up User Action";
                String END = "Signed Up";
            }

            public interface SignIn {
                String BEGIN = "Begin Sign In";
                String END = "Signed In";
            }

            public interface Confirmation {
                String BEGIN = "Display Summary";
                String END = "Checkout";
            }

            public interface Finalize {
                String BEGIN = "Display Thank you";
                String END = "Worklow Done";
            }

            String SIGNING_IN = "Signing In";
            String CONFIRMING = "Confirming order";
            String FINALIZING = "Finalizing";

        }

        public interface AddPaymentCardMethod {
            String CARD_SCANNED = "Payment Card Scanned";
            String CARD_NOT_SCANNED = "Payment Card Not Scanned";
        }

        public interface Activities {
            String SCREEN_SEEN = "Screen seen";
        }

        public interface UInterface {
            String SHOPELIA_BUTTON_SHOWN = "Shopelia button shown";

        }

        public String ADD_ADDRESS_METHOD = "AddAddressMethod";

    }

    public interface Properties {
        String SCREEN_NAME = "Screen Name";
        String EVENT_TIME = "Event Time";
        String STEP = "Step";
        String CLICK_ON = "Click On";
        String METHOD = "Method";

        public interface Steps {

            public interface SigningUp {
                String BEGIN = "Begin";
                String ACCOUNT_CREATED = "Account Created";
                String EMAIL = "Email OK";
                String PHONE = "Phone OK";
                String ADDRESS = "Address OK";
                String PAYMENT_CARD = "Payment Card OK";
                String PINCODE = "Pincode OK";
            }

        }

        public interface AddAddressMethod {
            String PLACES_AUTOCOMPLETE = "Places Autocompletion";
            String REVERSE_DIRECTORY = "Reverse Directory";
            String MANUAL = "Manual";
        }

        public interface ClickOn {

            public interface SigningUp {
                String EMAIL = "Email";
                String PHONE = "Phone";
                String ADDRESS = "Address";
                String ADDRESS_NAME = "Address First Name";
                String ADDRESS_LAST_NAME = "Address Last Name";
                String ADDRESS_LINE_1 = "Address Line 1";
                String ADDRESS_LINE_2 = "Address Line 2";
                String ADDRESS_ZIP = "Address Zip Code";
                String ADDRESS_COUNTRY = "Address Country";
                String ADDRESS_CITY = "Address City";
                String PINCODE = "Pincode";
                String PAYMENT_CARD = "Payment Card";
                String PAYMENT_NUMBER = "Payment Number";
                String PAYMENT_DATE = "Payment Expiry Date";
                String PAYMENT_CVV = "Payment CVV";
                String PAYMENT_SCANNED = "Payment Scanned";
            }

        }

    }

}
