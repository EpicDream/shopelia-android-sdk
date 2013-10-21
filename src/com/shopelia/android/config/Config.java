package com.shopelia.android.config;

import com.shopelia.android.BuildConfig;

/**
 * Config for Shopelio
 * 
 * @author Pierre Pollastri
 */
@SuppressWarnings("all")
public final class Config {

	private Config() {
	}

	// /////////////////////////////////////////////////////////////
	//
	// Compilation target
	//
	// /////////////////////////////////////////////////////////////

	public static final int COMPILATION_TARGET_RELEASE = 0;
	public static final int COMPILATION_TARGET_DEBUG = 1;

	/**
	 * The current compilation target
	 */
	public static final int COMPILATION_TARGET = BuildConfig.DEBUG ? COMPILATION_TARGET_DEBUG
			: COMPILATION_TARGET_RELEASE;

	public static final boolean DEBUG = BuildConfig.DEBUG;
	public static final boolean RELEASE = !DEBUG;

	// /////////////////////////////////////////////////////////////
	//
	// Logs
	//
	// /////////////////////////////////////////////////////////////

	private static final int LOG_LEVEL_INFO = 3;
	private static final int LOG_LEVEL_WARNING = 2;
	private static final int LOG_LEVEL_ERROR = 1;
	private static final int LOG_LEVEL_NONE = 0;

	/**
	 * Set this flag to LOG_LEVEL_NONE when releasing your application in order
	 * to remove all logs.
	 */
	private static final int LOG_LEVEL = (COMPILATION_TARGET == COMPILATION_TARGET_DEBUG) ? LOG_LEVEL_INFO
			: LOG_LEVEL_ERROR;

	/**
	 * Indicates whether info logs are enabled. This should be true only when
	 * developing/debugging an application/the library
	 */
	public static final boolean INFO_LOGS_ENABLED = (LOG_LEVEL == LOG_LEVEL_INFO);

	/**
	 * Indicates whether warning logs are enabled
	 */
	public static final boolean WARNING_LOGS_ENABLED = INFO_LOGS_ENABLED
			|| (LOG_LEVEL == LOG_LEVEL_WARNING);

	/**
	 * Indicates whether error logs are enabled. Error logs are usually always
	 * enabled, even in production releases.
	 */
	public static final boolean ERROR_LOGS_ENABLED = WARNING_LOGS_ENABLED
			|| (LOG_LEVEL == LOG_LEVEL_ERROR);

	public static final String PREFERENCES_NAME = "ShopeliaGlobalPreferences";

	// /////////////////////////////////////////////////////////////
	//
	// Extras
	//
	// /////////////////////////////////////////////////////////////

	public static final String EXTRA_PREFIX = "com.shopelia.android.extras.";

	// /////////////////////////////////////////////////////////////
	//
	// Token
	//
	// /////////////////////////////////////////////////////////////

	public final static String CARDIO_TOKEN = "d6d9251b267d40e9bbc608a51e63e728";

	// /////////////////////////////////////////////////////////////
	//
	// Activity request
	//
	// /////////////////////////////////////////////////////////////
	public static final int REQUEST_ORDER = 0x916;

	public static final String ACCOUNT_TYPE = "com.shopelia.android";
	public static final String AUTH_TOKEN_TYPE = "com.shopelia.android.standard";

	public static final String PUBLIC_DIRECTORY = "Shopelia/";
	public static final String PUBLIC_ASSETS_DIRECTORY = PUBLIC_DIRECTORY
			+ "assets/";
	public static final String PUBLIC_FONTS_DIRECTORY = PUBLIC_ASSETS_DIRECTORY
			+ "fonts/";

	public static final String DEFAULT_TRACKER = "shopelia-android";

}
