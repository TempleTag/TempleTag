package edu.temple.templetag.fragments;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

import edu.temple.templetag.HomeActivity;
import edu.temple.templetag.R;
import edu.temple.templetag.Tag;
import edu.temple.templetag.TagDetailActivity;
import edu.temple.templetag.tools.DistanceCalculator;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener {
    private static final String TAGS = "tags";
    private static final String TAG = "tag";
    private static final String CUR_LOC = "currentLocation";
    private Location currentLocation;
    private DistanceCalculator distanceCalculator = new DistanceCalculator();

    private ArrayList<Tag> tags;
    private Tag tag;
    private Context mContext;

    //Initiate Map Stuff
    MapView mapView;
    public GoogleMap googleMap;

    public MapFragment() {
        // Required empty public constructor
    }


    public static MapFragment newInstance(ArrayList<Tag> tags, Location currentLocation) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(TAGS, tags);
        args.putParcelable(CUR_LOC, currentLocation);
        fragment.setArguments(args);
        return fragment;
    }

    public static MapFragment newInstance(Tag tag, Location currentLocation) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putParcelable(TAG, tag);
        args.putParcelable(CUR_LOC, currentLocation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tags = getArguments().getParcelableArrayList(TAGS);
            if (tags == null){
                tag = getArguments().getParcelable(TAG);
            }
            currentLocation = getArguments().getParcelable(CUR_LOC);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context; // TODO: should we mirror in detach?
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

    private void displayMarkers(ArrayList<Tag> Tags){
        if (googleMap != null && currentLocation != null) {
            googleMap.clear();
            for (Tag tag : Tags){
                if (distanceCalculator.distanceFromTo(tag.getmTagLocationLat(), tag.getmTagLocationLong(), currentLocation.getLatitude(),
                        currentLocation.getLongitude()) < HomeActivity.MAX_RADIUS){ //if tag is close to the user --> display
                    LatLng latLng = new LatLng(tag.getmTagLocationLat(), tag.getmTagLocationLong());
                    if (tag.getmTagPopularity() < 3) {
                        // Default map marker size
                        int height = 100;
                        int width = 100;
                        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.bluemarker);
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                        // Add marker with custom marker icon
                        (googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(tag.getmTagLocationName())
                                .snippet(tag.getmTagDescription())
                                .icon(smallMarkerIcon))).setTag(tag);
                    }
                    else if (tag.getmTagPopularity() == 3) {
                        // Map marker should be larger
                        int height = 150;
                        int width = 150;
                        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.bluemarker);
                        Bitmap mediumMarker = Bitmap.createScaledBitmap(b, width, height, false);
                        BitmapDescriptor mediumMarkerIcon = BitmapDescriptorFactory.fromBitmap(mediumMarker);
                        // Add marker with custom marker icon
                        (googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(tag.getmTagLocationName())
                                .snippet(tag.getmTagDescription())
                                .icon(mediumMarkerIcon))).setTag(tag);
                    } else {
                        // Map marker should be even larger
                        int height = 200;
                        int width = 200;
                        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.bluemarker);
                        Bitmap largeMarker = Bitmap.createScaledBitmap(b, width, height, false);
                        BitmapDescriptor largeMarkerIcon = BitmapDescriptorFactory.fromBitmap(largeMarker);
                        // Add marker with custom marker icon
                        (googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(tag.getmTagLocationName())
                                .snippet(tag.getmTagDescription())
                                .icon(largeMarkerIcon))).setTag(tag);
                    }
                }
            }
            focusOn(currentLocation);
        }
    }

    private void displayAMarker(Tag tag){
        if (googleMap != null && currentLocation != null) {
            googleMap.clear();
            LatLng latLng = new LatLng(tag.getmTagLocationLat(), tag.getmTagLocationLong());
            if (tag.getmTagPopularity() < 3) {
                // Default map marker size
                int height = 85;
                int width = 85;
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.bluemarker);
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                // Add map marker with custom marker icon
                (googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(tag.getmTagLocationName())
                        .snippet(tag.getmTagDescription())
                        .icon(smallMarkerIcon))).setTag(tag);
            }
            else if (tag.getmTagPopularity() == 3) {
                // Map marker should be larger
                int height = 120;
                int width = 120;
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.bluemarker);
                Bitmap mediumMarker = Bitmap.createScaledBitmap(b, width, height, false);
                BitmapDescriptor mediumMarkerIcon = BitmapDescriptorFactory.fromBitmap(mediumMarker);
                // Add map marker with custom marker icon
                (googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(tag.getmTagLocationName())
                        .snippet(tag.getmTagDescription())
                        .icon(mediumMarkerIcon))).setTag(tag);
            } else {
                // Map marker should be even larger
                int height = 200;
                int width = 200;
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.bluemarker);
                Bitmap largeMarker = Bitmap.createScaledBitmap(b, width, height, false);
                BitmapDescriptor largeMarkerIcon = BitmapDescriptorFactory.fromBitmap(largeMarker);
                // Add map marker with custom marker icon
                (googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(tag.getmTagLocationName())
                        .snippet(tag.getmTagDescription())
                        .icon(largeMarkerIcon))).setTag(tag);
            }
            focusOn(currentLocation);
        }
    }

    public void updateNewTagsLocations(ArrayList<Tag> tags, Location currentLocation){
        this.tags = tags;
        this.currentLocation = currentLocation;
        displayMarkers(this.tags);
    }

    public void updateNewTagLocation(Tag tag, Location currentLocation){
        this.tag = tag;
        this.currentLocation = currentLocation;
        displayAMarker(this.tag);
    }

    public void focusOn(Location location){
        if (googleMap!= null && location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            googleMap.animateCamera(cameraUpdate);

            //This value is in miles so keep the radius somewhere near 1.6*the max radius in home activity
            java.util.List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dash(1));
            Circle circle = this.googleMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(400)
                    .strokeColor(Color.rgb(157,34,53)).strokePattern(pattern).fillColor(Color.parseColor("#22c499a0")));
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
        if (tags != null) {
            displayMarkers(this.tags);
        } else if (tag != null){
            displayAMarker(this.tag);
        }
        this.googleMap.setOnInfoWindowClickListener(this);
//        this.googleMap.setMinZoomPreference(7);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent(mContext, TagDetailActivity.class);
        intent.putExtra("theTag", ((Tag)marker.getTag()));
        mContext.startActivity(intent);
    }
}
