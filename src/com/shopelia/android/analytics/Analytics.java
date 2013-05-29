package com.shopelia.android.analytics;

public interface Analytics {

    public interface Events {

        public interface Steps {

            public interface SignUp {
                String BEGIN = "Begin Sign Up";
                String SIGNING_UP = "Signing Up";
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

        public interface Activities {
            String SCREEN_SEEN = "Screen seen";
        }

        public interface UInterface {
            String SHOPELIA_BUTTON_SHOWN = "Shopelia button shown";
            String FIELD_FILLED = "FieldFilled";

        }

    }

    public interface Properties {
        String SCREEN_NAME = "Screen Name";
        String STEP = "Step";

        public interface Steps {

            public interface SigningUp {
                String BEGIN = "Begin";
                String ACCOUNT_CREATED = "Account Created";
                String EMAIL = "Email OK";
                String PHONE = "Phone OK";
                String ADDRESS = "Address OK";
                String PAYMENT_CARD = "Payment Card OK";
                String PINCODE = "Pincode OK";
                String PINCODE_CANCEL = "Pincode Canceled";
            }

        }

    }

}
