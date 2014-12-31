package com.thosedays.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thosedays.model.Event;
import com.thosedays.model.GeoPoint;
import com.thosedays.model.Photo;
import com.thosedays.provider.EventContract;
import com.thosedays.ui.widget.ExpandingScrollView;

import java.util.ArrayList;

import joey.thosedays.R;

import static com.thosedays.util.LogUtils.LOGD;
import static com.thosedays.util.LogUtils.makeLogTag;

/**
 * Created by joey on 14/11/20.
 */
public class MapFragment extends com.google.android.gms.maps.MapFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, GoogleMap.OnMarkerClickListener {

    private static final String TAG = makeLogTag(MapFragment.class);
    private static final int EVENT_LOADER = 1;

    private static final String EXTRA_BOUNDS = "bounds";

    private GoogleMap mMap;
    private ExpandingScrollView mSideUpPanel;
    private ArrayList<Event> mEvents;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Nested fragments are only supported when added to a fragment dynamically.
        View mapView = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ViewGroup layout = (ViewGroup) view.findViewById(R.id.map_container);
        layout.addView(mapView, 0);
        mapView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        mMap = getMap();
        mMap.setOnMarkerClickListener(this);
        mSideUpPanel = (ExpandingScrollView) view.findViewById(R.id.slideup_panel);
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_BOUNDS, mMap.getProjection().getVisibleRegion().latLngBounds);

        getLoaderManager().initLoader(EVENT_LOADER, args, this);
        return view;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        switch (i) {
            case EVENT_LOADER:
                LatLngBounds bounds = bundle.getParcelable(EXTRA_BOUNDS);
                Uri uri = EventContract.Events.buildEventsLocationUri(bounds);
                return new CursorLoader(
                        activity,   // Parent activity context
                        uri,
                        EventContract.Events.DEFAULT_PROJECTIONS,     // Projection to return
                        EventContract.Events.DEFAULT_SELECTION,            // No selection clause
                        null,            // No selection arguments
                        EventContract.Events.SORT_BY_EVENT_TIME             // Default sort order
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (mEvents == null)
            mEvents = new ArrayList<Event>();
        mEvents.clear();
        mMap.clear();
        while (cursor.moveToNext()) {
            Event event = new Event();
            event.description = cursor.getString(cursor.getColumnIndex(EventContract.Events.EVENT_DESCRIPTION));
            Photo photo = new Photo();
            photo.thumb_url = cursor.getString(cursor.getColumnIndex(EventContract.Events.PHOTO_URL));
            photo.width = cursor.getInt(cursor.getColumnIndex(EventContract.Events.PHOTO_WIDTH));
            photo.height = cursor.getInt(cursor.getColumnIndex(EventContract.Events.PHOTO_HEIGHT));
            event.photo = photo;
            GeoPoint point = new GeoPoint();
            point.lat = cursor.getDouble(cursor.getColumnIndex(EventContract.Events.LOC_LAT));
            point.lng = cursor.getDouble(cursor.getColumnIndex(EventContract.Events.LOC_LNG));
            event.location = point;
            mEvents.add(event);
            mMap.addMarker(new MarkerOptions().position(new LatLng(point.lat, point.lng)));
        }
        cursor.close();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mEvents.clear();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LOGD(TAG, "onMarkerClick");
        mSideUpPanel.scrollTo(0, 200);
        return true;
    }
}
