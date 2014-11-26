package com.thosedays.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import joey.thosedays.R;

/**
 * Created by joey on 14/11/20.
 */
public class MapFragment extends com.google.android.gms.maps.MapFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Nested fragments are only supported when added to a fragment dynamically.
        View mapView = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ViewGroup layout = (ViewGroup) view.findViewById(R.id.map_container);
        layout.addView(mapView, 0);
        mapView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500));
        return view;
    }
}
