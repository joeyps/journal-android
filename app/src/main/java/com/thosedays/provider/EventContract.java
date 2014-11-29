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
        /** Unique string identifying this event. */
        String EVENT_ID = "event_id";
        String EVENT_DESCRIPTION = "description";
        String PHOTO_URL = "photo_url";
        String PHOTO_WIDTH = "photo_width";
        String PHOTO_HEIGHT = "photo_height";
        String TAGS = "tags";
        String EVENT_TIME = "event_time";
        String DELETED = "deleted";
        String SYNCED = "synced";
    }

    private static final String PATH_EVENTS = "events";
    private static final String PATH_MESSAGES = "messages";

    public static final String[] TOP_LEVEL_PATHS = {
            PATH_EVENTS,
            PATH_MESSAGES
    };

    public static class Events implements EventColumns, BaseColumns {
        public static final String BLOCK_TYPE_FREE = "free";
        public static final String BLOCK_TYPE_BREAK = "break";
        public static final String BLOCK_TYPE_KEYNOTE = "keynote";

        // ORDER BY clauses
        public static final String DEFAULT_SELECTION = DELETED + "=0";
        public static final String SORT_BY_EVENT_TIME = EVENT_TIME + " DESC";

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

