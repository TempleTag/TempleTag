package edu.temple.templetag.fragments;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import edu.temple.templetag.R;
import edu.temple.templetag.Tag;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String ARG_PARAM1 = "tags";
    private static final String ARG_PARAM2 = "currentLocation";

    // TODO: Rename and change types of parameters
    private ArrayList<Tag> tags;
    private LatLng currentUserLocation;

    //Initiate Map Stuff
    MapView mapView;
    GoogleMap googleMap;

    public MapFragment() {
        // Required empty public constructor
    }


    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
//        Bundle args = new Bundle();
//        args.putParcelableArrayList(ARG_PARAM1, tags);
//        args.putParcelable(ARG_PARAM2, currentUserLocation);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            tags = getArguments().getParcelableArrayList(ARG_PARAM1);
//            currentUserLocation = getArguments().getParcelable(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        //Setup MapView
        if (v.findViewById(R.id.mapView) != null){
            mapView = v.findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        } else {
            Log.d("map","onCreateView Error");
        }
        return v;
    }

    public void updateMap(ArrayList<Tag> tags){
        if (googleMap != null) {
            googleMap.clear();
            for (int i = 0; i < tags.size(); i++) {
                Tag thisTag = tags.get(i);
                LatLng loc = new LatLng(thisTag.getmTagLocationLat(), thisTag.getmTagLocationLong());
                googleMap.addMarker(new MarkerOptions().position(loc).title(thisTag.getmTagDescription()));
            }
        }
    }

    public void focusOn(LatLng currentUserLocation){
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15);
        googleMap.animateCamera(cameraUpdate);
    }



    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
//        updateMap(tags);
//        focusOn(currentUserLocation);
    }
}
