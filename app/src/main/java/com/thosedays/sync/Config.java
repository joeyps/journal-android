package com.thosedays.sync;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by joey on 14/11/6.
 */
public class Config {

    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.thosedays.sync.provider";

    public static final String HOST = "https://those-days.appspot.com";
    public static final String AUTH_URL = HOST + "/_auth";
    public static final String API_URL = HOST + "/_api";

    // Manifest URL
    public static final String MANIFEST_URL = HOST + "/_sync/manifest";

    public static final String EXTRA_ACCOUNT = "account";

    //http request
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_MODIFIED_SINCE = "If-Modified-Since";
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    private static final SimpleDateFormat VALID_IFMODIFIEDSINCE_FORMAT =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    //authorization
    public static final String AUTH_TYPE = "auth";

    public static boolean isValidFormatForIfModifiedSinceHeader(String timestamp) {
        try {
            return VALID_IFMODIFIEDSINCE_FORMAT.parse(timestamp)!=null;
        } catch (Exception ex) {
            return false;
        }
    }
}
