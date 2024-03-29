package com.shopelia.android.config;

public final class Build {

    private Build() {

    }

    public static final String SDK = "Android";

    public static final class VERSION {
        public static final int SDK_INT = VERSION_CODES.ALBENIZ_MR1;
        public static final String RELEASE = VERSIONS.ALBENIZ_MR1;
    }

    public static final class VERSION_CODES {

        // Version 1.0
        public static final int ALBENIZ = 1;
        public static final int ALBENIZ_MR1 = 2;

    }

    public static final class VERSIONS {

        public static final String ALBENIZ = "1.0";
        public static final String ALBENIZ_MR1 = "1.0.1";

    }

}
