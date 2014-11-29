package com.thosedays.util;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.net.Uri;

import com.thosedays.provider.EventContract;
import com.thosedays.sync.SyncHelper;

import static com.thosedays.util.LogUtils.LOGD;
import static com.thosedays.util.LogUtils.makeLogTag;

/**
 * Created by joey on 14/11/29.
 */
public class EventHelper {

    private static final String TAG = makeLogTag(EventHelper.class);

    private final Activity mActivity;

    public EventHelper(Activity activity) {
        mActivity = activity;
    }

    public void markEventAsDeleted(String eventId) {
        LOGD(TAG, "markEventAsDeleted eventId=" + eventId);
        Uri uri = EventContract.Events.buildEventUri(eventId);

        AsyncQueryHandler handler =
                new AsyncQueryHandler(mActivity.getContentResolver()) {
                };
        final ContentValues values = new ContentValues();
        values.put(EventContract.Events.DELETED, 1);
        values.put(EventContract.Events.SYNCED, 0);
        handler.startUpdate(-1, null, uri, values, null, null);

        // Request an immediate user data sync to reflect the starred user sessions in the cloud
        SyncHelper.requestManualSync(AccountUtils.getActiveAccount(mActivity), true);

    }
}
