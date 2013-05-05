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

        public static final class Phones {
            public static final String $ = V1.$ + "/phones";

            public static String Lookup(String number) {
                return Phones.$ + "/" + number + "/lookup";
            }
        }

        public static final class PaymentCards {
            public static final String $ = V1.$ + "/payment_cards";
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

        }
    }

}
