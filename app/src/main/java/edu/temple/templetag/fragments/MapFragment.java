package edu.temple.templetag.fragments;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import edu.temple.templetag.HomeActivity;
import edu.temple.templetag.R;
import edu.temple.templetag.Tag;
import edu.temple.templetag.tools.DistanceCalculator;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String ARG_PARAM1 = "tags";
    private static final String ARG_PARAM2 = "currentLocation";
    private Location currentLocation;
    private DistanceCalculator distanceCalculator = new DistanceCalculator();

    // TODO: Rename and change types of parameters
    private ArrayList<Tag> tags;

    //Initiate Map Stuff
    MapView mapView;
    GoogleMap googleMap;

    public MapFragment() {
        // Required empty public constructor
    }


    public static MapFragment newInstance(Location currentLocation) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM2, currentLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentLocation = getArguments().getParcelable(ARG_PARAM2);
        }
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

    public void displayMarkers(ArrayList<Tag> Tags, Location currentLocation){
        this.currentLocation = currentLocation;

        if (googleMap != null && currentLocation != null) {
            googleMap.clear();
            for (Tag tag : Tags){
                if (distanceCalculator.distanceFromTo(tag.getmTagLocationLat(), tag.getmTagLocationLong(), currentLocation.getLatitude(),
                        currentLocation.getLongitude()) < HomeActivity.MAX_RADIUS){ //if tag is close to the user --> display
                    LatLng latLng = new LatLng(tag.getmTagLocationLat(), tag.getmTagLocationLong());
                    googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(tag.getmTagLocationName())
                            .snippet(tag.getmTagDescription())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                }
            }
            //Create user location marker
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("You are here!")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            focusOn(currentLocation);
        }
    }

    public void focusOn(Location location){
        if (googleMap!= null && location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            googleMap.animateCamera(cameraUpdate);
        }
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
        focusOn(currentLocation);
    }
}
