package com.thosedays.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.thosedays.provider.EventContract;

import joey.thosedays.R;

/**
 * Created by joey on 14/11/12.
 */
public class EventDetailActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifies a particular Loader being used in this component
    private static final int URL_LOADER = 0;

    private ViewPager mPager;
    private FragmentStatePagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        mPager = (ViewPager) findViewById(R.id.pager);
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,   // Parent activity context
                        EventContract.Events.CONTENT_URI,        // Table to query
                        new String[] {
                                EventContract.Events._ID,
                                EventContract.Events.EVENT_ID,
                                EventContract.Events.EVENT_DESCRIPTION,
                                EventContract.Events.PHOTO_URL,
                                EventContract.Events.PHOTO_WIDTH,
                                EventContract.Events.PHOTO_HEIGHT,
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
        if (mAdapter == null) {
            mAdapter = new EventPagerAdapter(getSupportFragmentManager(), cursor);
            mPager.setAdapter(mAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private class EventPagerAdapter extends FragmentStatePagerAdapter {

        private Cursor mCursor;

        public EventPagerAdapter(FragmentManager fm, Cursor cursor) {
            super(fm);
            mCursor = cursor;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new EventDetailFragment();
            mCursor.moveToPosition(position);
            fragment.setArguments(convertToArguments(mCursor));
            return fragment;
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        private Bundle convertToArguments(Cursor cursor) {
            Bundle args = new Bundle();
            args.putString(EventContract.Events._ID, cursor.getString(cursor.getColumnIndex(EventContract.Events._ID)));
            args.putString(EventContract.Events.EVENT_ID, cursor.getString(cursor.getColumnIndex(EventContract.Events.EVENT_ID)));
            args.putString(EventContract.Events.EVENT_DESCRIPTION, cursor.getString(cursor.getColumnIndex(EventContract.Events.EVENT_DESCRIPTION)));
            args.putString(EventContract.Events.PHOTO_URL, cursor.getString(cursor.getColumnIndex(EventContract.Events.PHOTO_URL)));
            args.putInt(EventContract.Events.PHOTO_WIDTH, cursor.getInt(cursor.getColumnIndex(EventContract.Events.PHOTO_WIDTH)));
            args.putInt(EventContract.Events.PHOTO_HEIGHT, cursor.getInt(cursor.getColumnIndex(EventContract.Events.PHOTO_HEIGHT)));
            return args;
        }
    }

}
