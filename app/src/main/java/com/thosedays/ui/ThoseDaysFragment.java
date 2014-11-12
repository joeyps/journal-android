package com.thosedays.ui;

import android.accounts.Account;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.thosedays.provider.EventContract;
import com.thosedays.sync.Config;
import com.thosedays.util.ImageLoader;

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

    private CursorAdapter mAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAccount = getArguments().getParcelable(Config.EXTRA_ACCOUNT);
        // Inflate the layout for this fragment
        ListView listview = (ListView) inflater.inflate(R.layout.fragment_thosedays, container, false);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), EventDetailActivity.class);
                startActivity(intent);
            }
        });
        mAdapter = new EventCursorAdapter(
            getActivity(),                // Current context
            null,                // No Cursor yet
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

    private static class ViewHolder implements ImageLoader.ImageHolder {

        private ImageView imageView;
        private int width;

        @Override
        public void setImage(Bitmap bitmap, String key) {
            if (key.equals(imageView.getTag())) {
                imageView.setImageBitmap(bitmap);
                imageView.setTag(null);
            }
        }
    }

    private static class EventCursorAdapter extends CursorAdapter {

        private Activity mActivity;
        private int mListViewPadding;

        public EventCursorAdapter(Activity activity, Cursor c, int flags) {
            super(activity, c, 0);
            mActivity = activity;
            mListViewPadding = mActivity.getResources().getDimensionPixelSize(R.dimen.cardview_spacing);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View view = mActivity.getLayoutInflater().inflate(R.layout.event_list_item, viewGroup, false);
            ViewHolder vh = new ViewHolder();
            vh.imageView = (ImageView) view.findViewById(R.id.img_photo);
            vh.width = viewGroup.getWidth();
            view.setTag(vh);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder vh = (ViewHolder) view.getTag();
            TextView textView = (TextView) view.findViewById(R.id.text_description);
            textView.setText(cursor.getString(cursor.getColumnIndex(EventContract.Events.EVENT_DESCRIPTION)));
            int expectedWidth = vh.width - mListViewPadding - mListViewPadding;
            int width = cursor.getInt(cursor.getColumnIndex(EventContract.Events.PHOTO_WIDTH));
            int height = cursor.getInt(cursor.getColumnIndex(EventContract.Events.PHOTO_HEIGHT));
            if (width != 0)
                height = (int) ((((float)expectedWidth) / width) * height);
            else
                height = 0;
            vh.imageView.setMinimumHeight(height);
            vh.imageView.setImageBitmap(null);
            String url = cursor.getString(cursor.getColumnIndex(EventContract.Events.PHOTO_URL));
            if (url != null) {
                url = url + "700";
                String key = ImageLoader.load(vh, url);
                vh.imageView.setTag(key);
            } else {
                vh.imageView.setTag(null);
            }

        }
    }
}
