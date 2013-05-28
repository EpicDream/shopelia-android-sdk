package com.shopelia.android.analytics;

public interface Analytics {

    public interface Events {

        public interface Activities {
            String WORKFLOW_NEXT_STEP = "WorkflowNextStep";
        }

        public interface UInterface {
            String BUTTON_SHOWN = "ShopeliaButtonShown";
            String FIELD_FILLED = "FieldFilled";

        }

    }

    public interface Properties {
        String TIME_SPENT = "time_spent";
        String FIELD_NAME = "field_name";
        String ACTIVITY_NAME = "activity_name";
    }

}
