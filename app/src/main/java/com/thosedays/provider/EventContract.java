package com.thosedays.provider;

import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by joey on 14/11/7.
 */
public class EventContract {

    public static final String CONTENT_AUTHORITY = "com.thosedays.eventprovider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final int INVALID_LOCATION = -10000;

    interface EventColumns {
        /** Unique string identifying this event. */
        String EVENT_ID = "event_id";
        String EVENT_DESCRIPTION = "description";
        String PHOTO_URL = "photo_url";
        String PHOTO_WIDTH = "photo_width";
        String PHOTO_HEIGHT = "photo_height";
        String LOC_LAT = "loc_lat";
        String LOC_LNG = "loc_lng";
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

        public static final String QUERY_PARAMETER_BOUNDS_NORTH_EAST = "ne";
        public static final String QUERY_PARAMETER_BOUNDS_SOUTH_WEST = "sw";

        public static final String[] DEFAULT_PROJECTIONS = new String[] {
                Events._ID,
                Events.EVENT_ID,
                Events.EVENT_DESCRIPTION,
                Events.LOC_LAT,
                Events.LOC_LNG,
                Events.PHOTO_URL,
                Events.PHOTO_WIDTH,
                Events.PHOTO_HEIGHT,
                Events.EVENT_TIME
        };
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

        public static Uri buildEventsLocationUri(LatLngBounds bounds) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(QUERY_PARAMETER_BOUNDS_NORTH_EAST,
                            String.format("%s,%s", bounds.northeast.latitude, bounds.northeast.longitude))
                    .appendQueryParameter(QUERY_PARAMETER_BOUNDS_SOUTH_WEST,
                            String.format("%s,%s", bounds.southwest.latitude, bounds.southwest.longitude))
                    .build();
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

