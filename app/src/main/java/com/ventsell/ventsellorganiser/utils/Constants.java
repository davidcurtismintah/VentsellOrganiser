package com.ventsell.ventsellorganiser.utils;

public final class Constants {

    private Constants() {
    }

    public static final String KEY_LOGIN_TYPE = "login_type";

    public static final int LOGIN_TYPE_EMAIL = 2001;
    public static final int LOGIN_TYPE_FACEBOOK = 2002;
    public static final int LOGIN_TYPE_GOOGLE_PLUS = 2003;

    public static final class Scan {

        public static final String SAVE_HISTORY = "save_history";
        public static final String EXTRA_TICKET_ID = "ticket_id";

        private Scan() {
        }
    }

    public class History {
        public static final String ITEM_NUMBER = "item_number";

        private History() {
        }
    }
}
