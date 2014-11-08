package com.thosedays.sync;

import android.accounts.Account;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import java.io.IOException;

import static com.thosedays.util.LogUtils.LOGI;
import static com.thosedays.util.LogUtils.makeLogTag;

/**
 * Created by joey on 14/11/7.
 */
public class SyncHelper {

    private static final String TAG = makeLogTag(SyncHelper.class);

    private Context mContext;
    private RemoteDataHandler mDataHandler;

    public SyncHelper(Context context) {
        mContext = context;
        mDataHandler = new RemoteDataHandler(context);
    }

    public boolean performSync(SyncResult syncResult, Account account, Bundle extras) {
        try {
            doRemoteSync(account);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Checks if the remote server has new data that we need to import. If so, download
     * the new data and import it into the database.
     *
     * @return Whether or not data was changed.
     * @throws java.io.IOException if there is a problem downloading or importing the data.
     */
    private boolean doRemoteSync(Account account) throws IOException {
//        if (!isOnline()) {
//            LOGD(TAG, "Not attempting remote sync because device is OFFLINE");
//            return false;
//        }

        //LOGD(TAG, "Starting remote sync.");
        RemoteDataFetcher remoteDataFetcher = new RemoteDataFetcher(mContext, account);
        // Fetch the remote data files via RemoteConferenceDataFetcher
        RawData[] dataFiles = remoteDataFetcher.fetchDataIfNewer("");
                //mDataHandler.getDataTimestamp());

        if (dataFiles != null) {
            LOGI(TAG, "Applying remote data.");
            // save the remote data to the database
            mDataHandler.applyData(dataFiles, "", true);
//            mDataHandler.applyData(dataFiles, mRemoteDataFetcher.getServerDataTimestamp(), true);
            LOGI(TAG, "Done applying remote data.");

            // mark that conference data sync succeeded
            //PrefUtils.markSyncSucceededNow(mContext);
            return true;
        } else {
            // no data to process (everything is up to date)

            // mark that conference data sync succeeded
            //PrefUtils.markSyncSucceededNow(mContext);
            return false;
        }
    }

}
