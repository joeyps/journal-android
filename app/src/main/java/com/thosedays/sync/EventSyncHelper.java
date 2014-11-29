/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thosedays.sync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.thosedays.model.Event;
import com.thosedays.model.Photo;
import com.thosedays.provider.EventContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thosedays.util.LogUtils.LOGD;
import static com.thosedays.util.LogUtils.LOGE;
import static com.thosedays.util.LogUtils.LOGI;
import static com.thosedays.util.LogUtils.makeLogTag;

/**
 * Created by joey on 23/11/8.
 */
public class EventSyncHelper {
    private static final String TAG = makeLogTag(EventSyncHelper.class);

    private static final String DATA_DESCRIPTION = "desc";
    private static final String DATA_PHOTO_ID = "pid";
    private static final String DATA_EVENT_TIME = "event_time";

    private Context mContext;
    private WebServiceApi mApi;

    EventSyncHelper(Context context, Account account) {
        mContext = context;
        mApi = new WebServiceApi(context, account);

    }

    public boolean sync() {
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = EventContract.Events.CONTENT_URI;
        Cursor c = cr.query(uri,
                null,
                EventContract.Events.SYNCED + " = 0",
                null,
                null);
        LOGD(TAG, "Number of unsynced data: " + c.getCount());
        List<String> questions = new ArrayList<String>();
        Map<String, Event> updated = new HashMap<String, Event>();
        List<String> deletedList = new ArrayList<String>();

        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex(EventContract.Events._ID));
            int deleted = c.getInt(c.getColumnIndex(EventContract.Events.DELETED));
            if (deleted == 0) {
                Event event = uploadToServer(c);
                if (event != null) {
                    LOGI(TAG, "Successfully updated id " + id);

                    updated.put(id, event);
                } else {
                    LOGE(TAG, "Couldn't update id " + id);
                }
            } else {
                String eventId = c.getString(c.getColumnIndex(EventContract.Events.EVENT_ID));
                if (TextUtils.isEmpty(eventId)) {
                    //haven't synced to server yet
                    deletedList.add(id);
                } else if (mApi.deleteDataFromServer(WebServiceApi.API_EVENT, eventId) != null) {
                    deletedList.add(id);
                }
            }
        }
        c.close();

        updateEventsToDatabase(cr, updated);
        deleteEventsFromDatabase(cr, deletedList);

        return updated.size() > 0 || deletedList.size() > 0;
    }

    private Event uploadToServer(Cursor c) {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put(DATA_DESCRIPTION, c.getString(c.getColumnIndex(EventContract.Events.EVENT_DESCRIPTION)));
        data.put(DATA_EVENT_TIME, c.getString(c.getColumnIndex(EventContract.Events.EVENT_TIME)));

        String fileUri = c.getString(c.getColumnIndex(EventContract.Events.PHOTO_URL));
        Photo photo = null;
        String response = null;
        if (!TextUtils.isEmpty(fileUri)) {
            response = mApi.sendFileToServer(WebServiceApi.API_PHOTO, Uri.parse(fileUri));
            photo = new Gson().fromJson(response, Photo.class);
            data.put(DATA_EVENT_TIME, photo.utc);
        }

        if (photo != null)
            data.put(DATA_PHOTO_ID, photo.id);
        response = mApi.sendDataToServer(WebServiceApi.API_EVENT, data);
        if (response != null) {
            return new Gson().fromJson(response, Event.class);
        }
        return null;
    }

    private void updateEventsToDatabase(ContentResolver cr, Map<String, Event> updated) {
        // Flip the "synced" flag to true for any successfully updated sessions, but leave them
        // in the database to prevent duplicate feedback
        for (Map.Entry<String, Event> e : updated.entrySet()) {
            Event event = e.getValue();
            ContentValues contentValues = new ContentValues();
            contentValues.put(EventContract.Events.EVENT_ID, event.id);
            if (event.photo != null) {
                contentValues.put(EventContract.Events.PHOTO_URL, event.photo.thumb_url);
                contentValues.put(EventContract.Events.PHOTO_WIDTH, event.photo.width);
                contentValues.put(EventContract.Events.PHOTO_HEIGHT, event.photo.height);
            } else {
                contentValues.put(EventContract.Events.PHOTO_URL, "");
                contentValues.put(EventContract.Events.PHOTO_WIDTH, 0);
                contentValues.put(EventContract.Events.PHOTO_HEIGHT, 0);
            }
            contentValues.put(EventContract.Events.EVENT_TIME, event.event_time);
            contentValues.put(EventContract.Events.SYNCED, 1);
            cr.update(EventContract.Events.buildEventUri(e.getKey()), contentValues, null, null);
        }
    }

    private void deleteEventsFromDatabase(ContentResolver cr, List<String> deleted) {
        for (String id : deleted) {
            cr.delete(EventContract.Events.buildEventUri(id), null, null);
        }
    }
}
