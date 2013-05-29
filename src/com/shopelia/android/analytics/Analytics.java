package com.shopelia.android.analytics;

public interface Analytics {

    public interface Events {

        public interface Steps {
            String SIGNING_UP = "Signing Up";
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
        String SCREEN_NAME = "screen_name";
        String TIME_SPENT = "time_spent";
        String FIELD_NAME = "field_name";
        String ACTIVITY_NAME = "activity_name";
    }

}
