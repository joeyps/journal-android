package com.thosedays.model.handler;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.thosedays.model.Event;
import com.thosedays.provider.EventContract;

import java.util.ArrayList;

/**
 * Created by joey on 14/11/8.
 */
public class EventsHandler extends JSONHandler {

    private ArrayList<Event> mData = new ArrayList<Event>();

    public EventsHandler(Context context) {
        super(context);
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        for (Event block : mData) {
            output(block, list);
        }
    }

    @Override
    public void process(String json) {
        for (Event block : new Gson().fromJson(json, Event[].class)) {
            mData.add(block);
        }
    }

    private void output(Event event, ArrayList<ContentProviderOperation> list) {
        Uri uri = EventContract.addCallerIsSyncAdapterParameter(
                EventContract.Events.CONTENT_URI);
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
//        String title = block.title != null ? block.title : "";
//        String meta = block.subtitle != null ? block.subtitle : "";
//
//        long startTimeL = ParserUtils.parseTime(block.start);
//        long endTimeL = ParserUtils.parseTime(block.end);
//        final String blockId = EventContract.Events.generateBlockId(startTimeL, endTimeL);
        builder.withValue(EventContract.Events.EVENT_ID, event.id);
        builder.withValue(EventContract.Events.EVENT_DESCRIPTION, TextUtils.isEmpty(event.description) ? "" : unescape(event.description));
        builder.withValue(EventContract.Events.EVENT_TIME, event.event_time);
        if (event.photo != null) {
            builder.withValue(EventContract.Events.PHOTO_URL, event.photo.thumb_url);
            builder.withValue(EventContract.Events.PHOTO_WIDTH, event.photo.width);
            builder.withValue(EventContract.Events.PHOTO_HEIGHT, event.photo.height);
        } else {
            builder.withValue(EventContract.Events.PHOTO_URL, "");
            builder.withValue(EventContract.Events.PHOTO_WIDTH, 0);
            builder.withValue(EventContract.Events.PHOTO_HEIGHT, 0);
        }
        if (event.location != null) {
            builder.withValue(EventContract.Events.LOC_LAT, event.location.lat);
            builder.withValue(EventContract.Events.LOC_LNG, event.location.lng);
        } else {
            builder.withValue(EventContract.Events.LOC_LAT, EventContract.INVALID_LOCATION);
            builder.withValue(EventContract.Events.LOC_LNG, EventContract.INVALID_LOCATION);
        }
        builder.withValue(EventContract.Events.SYNCED, 1);
        list.add(builder.build());
    }

    private String unescape(String s) {
        return s.replace("&gt;", ">")
                .replace("&lt;", "<")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
    }
}
