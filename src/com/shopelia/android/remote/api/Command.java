package com.shopelia.android.remote.api;

/**
 * This class contains every command of the ShopeliaAPI
 * 
 * @author Pierre Pollastri
 */
public final class Command {

	private Command() {
		// Do not has to be instantiated
	}

	public static final class V1 {
		private static final String $ = "/api";

		public static final class Callback {
			public static final String $ = V1.$ + "/callback";

			public static String Orders(String uuid) {
				return Callback.$ + "/orders/" + uuid;
			}
		}

		public static final class Georges {
			public static final String $ = V1.$ + "/georges";

			public static String Messages = $ + "/messages";

		}

		public static final class Phones {
			public static final String $ = V1.$ + "/phone_lookup";

			public static String Lookup(String number) {
				return Phones.$ + "/" + number;
			}
		}

		public static final class PaymentCards {
			public static final String $ = V1.$ + "/payment_cards";

			public static String PaymentCard(long id) {
				return $ + "/" + id;
			}
		}

		public static final class Orders {
			public static final String $ = V1.$ + "/orders";

			public static String Order(String uuid) {
				return $ + "/" + uuid;
			}

		}

		public static final class Users {
			public static final String $ = V1.$ + "/users";

			public static final class SignIn {
				public static final String $ = Users.$ + "/sign_in";
			}

			public static final class Verify {
				public static final String $ = Users.$ + "/verify";
			}

			public static String User(long id) {
				return $ + "/" + id;
			}

			public static String Reset() {
				return $ + "/reset";
			}

			public static String Exists() {
				return $ + "/exists";
			}

			public static String SignOut() {
				return $ + "/sign_out";
			}

		}

		public static final class Addresses {
			public static final String $ = V1.$ + "/addresses";

			public static String Address(long id) {
				return $ + "/" + String.valueOf(id);
			}

		}

		public static final class Devices {
			public static final String $ = V1.$ + "/devices";

			public static String PushToken(String uuid) {
				return $ + "/" + uuid;
			}

		}

		public static final class Merchants {
			public static final String $ = V1.$ + "/merchants";
		}

		public static final class Events {
			public static final String $ = V1.$ + "/events";
		}

		public static final class Products {
			public static final String $ = V1.$ + "/products";
		}

		public static final class Traces {
			public static final String $ = V1.$ + "/traces";
		}

		public static final class Messages {
			public static final String $ = V1.$ + "/georges/messages";

			public static String Read(long id) {
				return $ + "/" + id + "/read";
			}

		}

	}

}
