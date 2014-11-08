package com.thosedays.fragment;

import android.accounts.Account;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.thosedays.provider.EventContract;
import com.thosedays.sync.Config;

import joey.thosedays.R;

import static com.thosedays.util.LogUtils.LOGD;

/**
 * Created by joey on 14/11/8.
 */
public class ThoseDaysFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifies a particular Loader being used in this component
    private static final int URL_LOADER = 0;

    private Account mAccount = null;

    public String[] mFromColumns = {
            EventContract.Events.EVENT_DESCRIPTION
    };
    public int[] mToFields = {
            R.id.text_description
    };

    SimpleCursorAdapter mAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mAccount = getArguments().getParcelable(Config.EXTRA_ACCOUNT);
        // Inflate the layout for this fragment
        ListView listview = (ListView) inflater.inflate(R.layout.fragment_thosedays, container, false);

        mAdapter = new SimpleCursorAdapter(
            getActivity(),                // Current context
            R.layout.event_list_item,  // Layout for a single row
            null,                // No Cursor yet
            mFromColumns,        // Cursor columns to use
            mToFields,           // Layout fields to use
            0                    // No flags
        );
        listview.setAdapter(mAdapter);
        getLoaderManager().initLoader(URL_LOADER, null, this);
        forceSync();
        return listview;
    }

    private void forceSync() {
        Bundle settingsBundle = new Bundle();
        //Forces a manual sync. The sync adapter framework ignores the existing settings
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        //Forces the sync to start immediately.
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(mAccount, Config.AUTHORITY, settingsBundle);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        /*
     * Takes action based on the ID of the Loader that's being created
     */
        switch (i) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        EventContract.Events.CONTENT_URI,        // Table to query
                        new String[] {
                            EventContract.Events._ID,
                            EventContract.Events.EVENT_ID,
                            EventContract.Events.EVENT_DESCRIPTION,
                            EventContract.Events.EVENT_TIME
                        },     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    /*
     * Moves the query results into the adapter, causing the
     * ListView fronting this adapter to re-display
     */
        mAdapter.changeCursor(cursor);
        LOGD("joey", "onLoadFinished");
    }

    /*
     * Invoked when the CursorLoader is being reset. For example, this is
     * called if the data in the provider changes and the Cursor becomes stale.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        /*
         * Clears out the adapter's reference to the Cursor.
         * This prevents memory leaks.
         */
        mAdapter.changeCursor(null);
    }
}
