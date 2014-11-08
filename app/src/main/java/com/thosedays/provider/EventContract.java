package com.thosedays.provider;

import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.text.TextUtils;

/**
 * Created by joey on 14/11/7.
 */
public class EventContract {

    public static final String CONTENT_AUTHORITY = "com.thosedays.eventprovider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    interface EventColumns {
        /** Unique string identifying this block of time. */
        String EVENT_ID = "event_id";
        /** Title describing this block of time. */
        String EVENT_DESCRIPTION = "description";
        /** Time when this block starts. */
        String EVENT_TIME = "event_time";
    }

    private static final String PATH_EVENTS = "events";

    public static class Events implements EventColumns, BaseColumns {
        public static final String BLOCK_TYPE_FREE = "free";
        public static final String BLOCK_TYPE_BREAK = "break";
        public static final String BLOCK_TYPE_KEYNOTE = "keynote";

        public static final boolean isValidBlockType(String type) {
            return BLOCK_TYPE_FREE.equals(type) || BLOCK_TYPE_BREAK.equals(type)
                    || BLOCK_TYPE_KEYNOTE.equals(type);
        }

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.thosedays.event";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.thosedays.event";

        /** Build {@link Uri} for requested {@link #EVENT_ID}. */
        public static Uri buildEventUri(String eventId) {
            return CONTENT_URI.buildUpon().appendPath(eventId).build();
        }

        /** Read {@link #EVENT_ID} from {@link Events} {@link Uri}. */
        public static String getEventId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(
                ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

    public static boolean hasCallerIsSyncAdapterParameter(Uri uri) {
        return TextUtils.equals("true",
                uri.getQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER));
    }
}

