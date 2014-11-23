package com.thosedays.sync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import java.io.IOException;

import static com.thosedays.util.LogUtils.LOGD;
import static com.thosedays.util.LogUtils.LOGE;
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
        boolean dataChanged = false;

        //try {
            final boolean userDataOnly = extras.getBoolean(SyncAdapter.EXTRA_SYNC_USER_DATA_ONLY, false);
            // remote sync consists of these operations, which we try one by one (and tolerate
            // individual failures on each)
            final int OP_REMOTE_SYNC = 0;
            final int OP_USER_DATA_SYNC = 1;
            final int OP_USER_FEEDBACK_SYNC = 2;

            int[] opsToPerform = userDataOnly ?
                    new int[] { OP_USER_DATA_SYNC } :
                    new int[] { OP_REMOTE_SYNC, OP_USER_DATA_SYNC, OP_USER_FEEDBACK_SYNC};

            for (int op : opsToPerform) {
                try {
                    switch (op) {
                        case OP_REMOTE_SYNC:
                            dataChanged |= doRemoteSync(account);;
                            break;
                        case OP_USER_DATA_SYNC:
                            dataChanged |= doUserScheduleSync(account);
                            break;
//                        case OP_USER_FEEDBACK_SYNC:
//                            doUserFeedbackSync();
//                            break;
                    }
                } /*catch (AuthException ex) {
                    syncResult.stats.numAuthExceptions++;

                    // if we have a token, try to refresh it
                    if (AccountUtils.hasToken(mContext, account.name)) {
                        AccountUtils.refreshAuthToken(mContext);
                    } else {
                        LOGW(TAG, "No auth token yet for this account. Skipping remote sync.");
                    }
                }*/ catch (Throwable throwable) {
                    throwable.printStackTrace();
                    LOGE(TAG, "Error performing remote sync.");
//                    increaseIoExceptions(syncResult);
                }
            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
        RawData[] dataFiles = remoteDataFetcher.fetchDataIfNewer(mDataHandler.getDataTimestamp());

        if (dataFiles != null) {
            LOGI(TAG, "Applying remote data.");
            // save the remote data to the database
            mDataHandler.applyData(dataFiles, remoteDataFetcher.getServerDataTimestamp(), true);
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

    /**
     * Checks if there are changes on MySchedule to sync with/from remote AppData folder.
     *
     * @return Whether or not data was changed.
     * @throws IOException if there is a problem uploading the data.
     */
    private boolean doUserScheduleSync(Account account) throws IOException {
//        if (!isOnline()) {
//            LOGD(TAG, "Not attempting myschedule sync because device is OFFLINE");
//            return false;
//        }

        LOGD(TAG, "Starting user data (myschedule) sync.");

        EventSyncHelper helper = new EventSyncHelper(mContext, account);
        boolean modified = helper.sync();
        if (modified) {
            // schedule notifications for the starred sessions
//            Intent scheduleIntent = new Intent(
//                    SessionAlarmService.ACTION_SCHEDULE_ALL_STARRED_BLOCKS,
//                    null, mContext, SessionAlarmService.class);
//            mContext.startService(scheduleIntent);
        }
        return modified;
    }

    public static void requestManualSync(Account mChosenAccount) {
        requestManualSync(mChosenAccount, false);
    }

    public static void requestManualSync(Account mChosenAccount, boolean userDataSyncOnly) {
        if (mChosenAccount != null) {
            LOGD(TAG, "Requesting manual sync for account " + mChosenAccount.name
                    +" userDataSyncOnly="+userDataSyncOnly);
            Bundle b = new Bundle();
            b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            if (userDataSyncOnly) {
                b.putBoolean(SyncAdapter.EXTRA_SYNC_USER_DATA_ONLY, true);
            }
            ContentResolver.setSyncAutomatically(mChosenAccount, Config.AUTHORITY, true);
            ContentResolver.setIsSyncable(mChosenAccount, Config.AUTHORITY, 1);

            boolean pending = ContentResolver.isSyncPending(mChosenAccount, Config.AUTHORITY);
            if (pending) {
                LOGD(TAG, "Warning: sync is PENDING. Will cancel.");
            }
            boolean active = ContentResolver.isSyncActive(mChosenAccount, Config.AUTHORITY);
            if (active) {
                LOGD(TAG, "Warning: sync is ACTIVE. Will cancel.");
            }

            if (pending || active) {
                LOGD(TAG, "Cancelling previously pending/active sync.");
                ContentResolver.cancelSync(mChosenAccount, Config.AUTHORITY);
            }

            LOGD(TAG, "Requesting sync now.");
            ContentResolver.requestSync(mChosenAccount, Config.AUTHORITY, b);
        } else {
            LOGD(TAG, "Can't request manual sync -- no chosen account.");
        }
    }

}
