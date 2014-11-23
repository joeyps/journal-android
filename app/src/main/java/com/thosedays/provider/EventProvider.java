package com.thosedays.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.thosedays.util.SelectionBuilder;

import java.util.ArrayList;

import static com.thosedays.util.LogUtils.LOGV;
import static com.thosedays.util.LogUtils.makeLogTag;

/**
 * Created by joey on 14/11/7.
 */
public class EventProvider extends ContentProvider {

    private static final String TAG = makeLogTag(EventProvider.class);

    private static final int EVENTS = 100;
    private static final int EVENTS_ID = 101;

    private EventDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        mOpenHelper = new EventDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        //String tagsFilter = uri.getQueryParameter(Sessions.QUERY_PARAMETER_TAG_FILTER);
        final int match = sUriMatcher.match(uri);

        // avoid the expensive string concatenation below if not loggable
//        if (Log.isLoggable(TAG, Log.VERBOSE)) {
//            LOGV(TAG, "uri=" + uri + " match=" + match + " proj=" + Arrays.toString(projection) +
//                    " selection=" + selection + " args=" + Arrays.toString(selectionArgs) + ")");
//        }


        switch (match) {
            default: {
                // Most cases are handled with simple SelectionBuilder
                final SelectionBuilder builder = buildExpandedSelection(uri, match);

                // If a special filter was specified, try to apply it
//                if (!TextUtils.isEmpty(tagsFilter)) {
//                    addTagsFilter(builder, tagsFilter);
//                }
                boolean distinct = false;
//                boolean distinct = !TextUtils.isEmpty(
//                        uri.getQueryParameter(ScheduleContract.QUERY_PARAMETER_DISTINCT));

                Cursor cursor = builder
                        .where(selection, selectionArgs)
                        .query(db, distinct, projection, sortOrder, null);
                Context context = getContext();
                if (null != context) {
                    cursor.setNotificationUri(context.getContentResolver(), uri);
                }
                return cursor;
            }
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EVENTS:
                return EventContract.Events.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        LOGV(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        boolean syncToNetwork = !EventContract.hasCallerIsSyncAdapterParameter(uri);
        switch (match) {
            case EVENTS: {
                db.insertOrThrow(EventDatabase.Tables.EVENTS, null, values);
                //notifyChange(uri);
                return EventContract.Events.buildEventUri(values.getAsString(EventContract.Events.EVENT_ID));
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//        String accountName = getCurrentAccountName(uri, false);
        LOGV(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).update(db, values);
//        notifyChange(uri);
        return retVal;
    }

    /**
     * Apply the given set of {@link android.content.ContentProviderOperation}, executing inside
     * a {@link android.database.sqlite.SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Build and return a {@link android.content.UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = EventContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "events", EVENTS);
        matcher.addURI(authority, "events/*", EVENTS_ID);

        return matcher;
    }

    /**
     * Build an advanced {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually only used by {@link #query}, since it
     * performs table joins useful for {@link Cursor} data.
     */
    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case EVENTS: {
                return builder.table(EventDatabase.Tables.EVENTS);
            }
            case EVENTS_ID: {
                final String eventId = EventContract.Events.getEventId(uri);
                return builder.table(EventDatabase.Tables.EVENTS)
                        .where(EventContract.Events.EVENT_ID + "=?", eventId);
            }
        }
        return null;
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EVENTS: {
                return builder.table(EventDatabase.Tables.EVENTS);
            }
            case EVENTS_ID: {
                final String eventId = EventContract.Events.getEventId(uri);
                return builder.table(EventDatabase.Tables.EVENTS)
                        .where(EventContract.Events._ID + "=?", eventId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for " + match + ": " + uri);
            }
        }
    }
}
