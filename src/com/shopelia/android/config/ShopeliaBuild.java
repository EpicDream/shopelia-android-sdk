package com.shopelia.android.config;

public final class ShopeliaBuild {

	private ShopeliaBuild() {

	}

	public static final String SDK = "Android";

	public static final class VERSION {
		public static final int SDK_INT = VERSION_CODES.ANONYMOUS;
		public static final String RELEASE = VERSIONS.ANONYMOUS_MR1;
	}

	public static final class VERSION_CODES {

		// Version 1.0
		public static final int ANONYMOUS = 1;
		public static final int ANONYMOUS_MR1 = 2;

	}

	public static final class VERSIONS {

		public static final String ANONYMOUS = "1.0";
		public static final String ANONYMOUS_MR1 = "1.0.1";

	}

}
