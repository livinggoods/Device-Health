package org.goods.living.tech.health.device.utils;

public final class Constants {

    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String DATA = "data";

    public static final long SYNC_FREQUENCY = 60 * 30;//60 * 60; // 1 hour (seconds)
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL = 300; // seconds.


    public final class URL {

        public static final String CREATE = "/create";
        public static final String READ = "/read";
        public static final String UPDATE = "/update";
        public static final String DELETE = "/delete";

        public static final String USER = "/user";
        public static final String USER_CREATE = USER + CREATE;
        public static final String USER_UPDATE = USER + UPDATE;
        //   public static final String USER_READ = USER + READ;
        // public static final String USER_UPDATE = USER + UPDATE;
        //  public static final String USER_DELETE = USER + DELETE;

        public static final String STATS = "/stats";
        public static final String STATS_CREATE = STATS + CREATE;
        //  public static final String STATS_READ = STATS + READ;
        //  public static final String STATS_UPDATE = STATS + UPDATE;
        //  public static final String STATS_DELETE = STATS + DELETE;

        public static final String DATABALANCE = "/databalance";
        public static final String DATABALANCE_CREATE = DATABALANCE + CREATE;
        public static final String DATABALANCE_USSDCODES = DATABALANCE + "/ussdCodes";
        public static final String USERS_REFRESH_TOKEN = USER + "/refreshToken";


    }
}
