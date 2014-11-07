package com.thosedays.sync;

/**
 * Created by joey on 14/11/6.
 */
public class Config {

    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.thosedays.sync.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "com.thosedays";

    public static final String HOST = "https://those-days.appspot.com";
    public static final String AUTH_URL = HOST + "/_auth";
    public static final String API_URL = HOST + "/_api";

    public static final String EXTRA_ACCOUNT = "account";
}
