package com.thosedays.sync;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import com.thosedays.model.handler.EventsHandler;
import com.thosedays.model.handler.JSONHandler;
import com.thosedays.provider.EventContract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.thosedays.util.LogUtils.LOGD;
import static com.thosedays.util.LogUtils.LOGE;
import static com.thosedays.util.LogUtils.makeLogTag;

/**
 * Created by joey on 14/11/8.
 */
public class RemoteDataHandler {

    private static final String TAG = makeLogTag(RemoteDataHandler.class);

    // Shared preferences key under which we store the timestamp that corresponds to
    // the data we currently have in our content provider.
    private static final String SP_KEY_DATA_TIMESTAMP = "data_timestamp";

    // symbolic timestamp to use when we are missing timestamp data (which means our data is
    // really old or nonexistent)
    private static final String DEFAULT_TIMESTAMP = "Sat, 1 Jan 2000 00:00:00 UTC";

    //data keys
    private static final String DATA_KEY_EVENTS = "events";
    private static final String[] DATA_KEYS_IN_ORDER = {
            DATA_KEY_EVENTS
    };



    private Context mContext = null;

    // Convenience map that maps the key name to its corresponding handler (e.g.
    // "blocks" to mBlocksHandler (to avoid very tedious if-elses)
    HashMap<String, JSONHandler> mHandlerForKey = new HashMap<String, JSONHandler>();

    public RemoteDataHandler(Context context) {
        mContext = context;
    }

    /**
     * Parses the conference data in the given objects and imports the data into the
     * content provider. The format of the data is documented at https://code.google.com/p/iosched.
     *
     * @param dataBodies The collection of JSON objects to parse and import.
     * @param dataTimestamp The timestamp of the data. This should be in RFC1123 format.
     * @param downloadsAllowed Whether or not we are supposed to download data from the internet if needed.
     * @throws java.io.IOException If there is a problem parsing the data.
     */
    public void applyData(RawData[] dataBodies, String dataTimestamp,
                                    boolean downloadsAllowed) throws IOException {
//        LOGD(TAG, "Applying data from " + dataBodies.length + " files, timestamp " + dataTimestamp);
//
        // create handlers for each data type
        mHandlerForKey.put(DATA_KEY_EVENTS, new EventsHandler(mContext));
//        mHandlerForKey.put(DATA_KEY_BLOCKS, mBlocksHandler = new BlocksHandler(mContext));
//        mHandlerForKey.put(DATA_KEY_TAGS, mTagsHandler = new TagsHandler(mContext));
//        mHandlerForKey.put(DATA_KEY_SPEAKERS, mSpeakersHandler = new SpeakersHandler(mContext));
//        mHandlerForKey.put(DATA_KEY_SESSIONS, mSessionsHandler = new SessionsHandler(mContext));
//        mHandlerForKey.put(DATA_KEY_SEARCH_SUGGESTIONS, mSearchSuggestHandler =
//                new SearchSuggestHandler(mContext));
//        mHandlerForKey.put(DATA_KEY_MAP, mMapPropertyHandler = new MapPropertyHandler(mContext));
//        mHandlerForKey.put(DATA_KEY_EXPERTS, mExpertsHandler = new ExpertsHandler(mContext));
//        mHandlerForKey.put(DATA_KEY_HASHTAGS, mHashtagsHandler = new HashtagsHandler(mContext));
//        mHandlerForKey.put(DATA_KEY_VIDEOS, mVideosHandler = new VideosHandler(mContext));
//        mHandlerForKey.put(DATA_KEY_PARTNERS, mPartnersHandler = new PartnersHandler(mContext));

        // process the jsons. This will call each of the handlers when appropriate to deal
        // with the objects we see in the data.
        LOGD(TAG, "Processing " + dataBodies.length + " JSON objects.");
        for (int i = 0; i < dataBodies.length; i++) {
            LOGD(TAG, "Processing json object #" + (i + 1) + " of " + dataBodies.length);
            processDataBody(dataBodies[i]);
        }

        // the sessions handler needs to know the tag and speaker maps to process sessions
//        mSessionsHandler.setTagMap(mTagsHandler.getTagMap());
//        mSessionsHandler.setSpeakerMap(mSpeakersHandler.getSpeakerMap());

        // produce the necessary content provider operations
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        for (String key : DATA_KEYS_IN_ORDER) {
            LOGD(TAG, "Building content provider operations for: " + key);
            mHandlerForKey.get(key).makeContentProviderOperations(batch);
            LOGD(TAG, "Content provider operations so far: " + batch.size());
        }
        LOGD(TAG, "Total content provider operations: " + batch.size());

        // download or process local map tile overlay files (SVG files)
        LOGD(TAG, "Processing map overlay files");
//        processMapOverlayFiles(mMapPropertyHandler.getTileOverlays(), downloadsAllowed);

        // finally, push the changes into the Content Provider
        LOGD(TAG, "Applying " + batch.size() + " content provider operations.");
        try {
            int operations = batch.size();
            if (operations > 0) {
                mContext.getContentResolver().applyBatch(EventContract.CONTENT_AUTHORITY, batch);
            }
            LOGD(TAG, "Successfully applied " + operations + " content provider operations.");
//            mContentProviderOperationsDone += operations;
        } catch (RemoteException ex) {
            LOGE(TAG, "RemoteException while applying content provider operations.");
            throw new RuntimeException("Error executing content provider batch operation", ex);
        } catch (OperationApplicationException ex) {
            LOGE(TAG, "OperationApplicationException while applying content provider operations.");
            throw new RuntimeException("Error executing content provider batch operation", ex);
        }

        // notify all top-level paths
        LOGD(TAG, "Notifying changes on all top-level paths on Content Resolver.");
        ContentResolver resolver = mContext.getContentResolver();
        for (String path : EventContract.TOP_LEVEL_PATHS) {
            Uri uri = EventContract.BASE_CONTENT_URI.buildUpon().appendPath(path).build();
            resolver.notifyChange(uri, null);
        }


        // update our data timestamp
        setDataTimestamp(dataTimestamp);
        LOGD(TAG, "Done applying conference data.");
    }

    /**
     * Processes a conference data body and calls the appropriate data type handlers
     * to process each of the objects represented therein.
     *
     * @param data The body of data to process
     * @throws IOException If there is an error parsing the data.
     */
    private void processDataBody(RawData data) throws IOException {
        if (mHandlerForKey.containsKey(data.type)) {
            mHandlerForKey.get(data.type).process(data.data);
        }
    }

    // Returns the timestamp of the data we have in the content provider.
    public String getDataTimestamp() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString(
                SP_KEY_DATA_TIMESTAMP, DEFAULT_TIMESTAMP);
    }

    // Sets the timestamp of the data we have in the content provider.
    public void setDataTimestamp(String timestamp) {
        LOGD(TAG, "Setting data timestamp to: " + timestamp);
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(
                SP_KEY_DATA_TIMESTAMP, timestamp).commit();
    }

    // Reset the timestamp of the data we have in the content provider
    public static void resetDataTimestamp(final Context context) {
        LOGD(TAG, "Resetting data timestamp to default (to invalidate our synced data)");
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(
                SP_KEY_DATA_TIMESTAMP).commit();
    }
}
