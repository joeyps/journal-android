package com.thosedays.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.thosedays.provider.EventContract;
import com.thosedays.util.EventHelper;

import joey.thosedays.R;

/**
 * Created by joey on 14/11/12.
 */
public class EventDetailActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>,
                                            ViewPager.OnPageChangeListener {

    public static final String EXTRA_DATA_INDEX = "extra_data_index";
    // Identifies a particular Loader being used in this component
    private static final int URL_LOADER = 0;

    private ViewPager mPager;
    private EventPagerAdapter mAdapter;
    private String mCurrentEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        setContentView(R.layout.activity_event_detail);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.pager_margin));
        mPager.setOnPageChangeListener(this);
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            if (mCurrentEventId != null) {
                EventHelper helper = new EventHelper(this);
                helper.markEventAsDeleted(mCurrentEventId);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                        EventContract.Events.DEFAULT_SELECTION,            // No selection clause
                        null,            // No selection arguments
                        EventContract.Events.SORT_BY_EVENT_TIME             // Default sort order
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
            int currentIndex = getIntent().getIntExtra(EXTRA_DATA_INDEX, 0);
            mPager.setCurrentItem(currentIndex);
        } else {
            mAdapter.changeCursor(cursor);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
        if (mAdapter != null)
            mCurrentEventId = mAdapter.getEventId(i);
    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private class EventPagerAdapter extends FragmentStatePagerAdapter {

        private Cursor mCursor;

        public EventPagerAdapter(FragmentManager fm, Cursor cursor) {
            super(fm);
            mCursor = cursor;
        }

        public void changeCursor(Cursor cursor) {
            if (mCursor != null)
                mCursor.close();
            mCursor = cursor;
        }

        public String getEventId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getString(mCursor.getColumnIndex(EventContract.Events._ID));
        }


        /**
         * For on dataset changed, try to correct fragment position
         * @param item
         * @return
         */
        @Override
        public int getItemPosition(Object item) {
            Fragment fragment = (Fragment)item;
            //find fragment position from current dataset
//            String title = fragment.getTitle();
//            int position = titles.indexOf(title);
            //TODO performance fine tune
            int position = POSITION_NONE;
            if (position >= 0) {
                return position;
            } else {
                return POSITION_NONE;
            }
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
            if (mCursor != null)
                return mCursor.getCount();
            return 0;
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
