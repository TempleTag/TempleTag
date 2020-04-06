package edu.temple.templetag;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.temple.templetag.fragments.MapFragment;
import edu.temple.templetag.fragments.TagRecyclerViewFragment;
import edu.temple.templetag.tools.DistanceCalculator;

public class HomeActivity extends AppCompatActivity {
    //Firebase and Firestore
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    //Views
    private FloatingActionButton createTagBtn;

    //Location
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FusedLocationProviderClient fusedLocationClient;
    public static Location currentLocation;

    //Fragments
    private MapFragment mapFragment;
    TagRecyclerViewFragment tagRecyclerViewFragment;

    //Tags
    private ArrayList<Tag> Tags = new ArrayList<>();

    //Distance Calculator
    DistanceCalculator distanceCalculator = new DistanceCalculator();

    //Other variables
    private Intent createTagIntent;
    private String txt_username, txt_email;
    public static final String TAG = "HomeActivity";
    public static final String TAG_LIST_FRAGMENT = "TagRecyclerFragment_HOME"; //This is the tag for TagRecyclerViewFragment for HomeActivity. This is not to be confused with the one in UserProfileActivity
    public static final int MAX_RADIUS = 2; //This is the radius of tags that will be displayed to the user

    @Override
    protected void onStart() {
        super.onStart();
        // Redirect user to LoginActivity if current active user is not found
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        createTagBtn = findViewById(R.id.createTagBtn);

        createTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateTagActivity();
            }
        });

        //Profile Icon On CLick
        CircleImageView profile = findViewById(R.id.userProfile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("username", txt_username);
                bundle.putString("email", txt_email);
                Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        //Check location permission
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }

        // Attach mapFragment
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragment");
        if (null != mapFragment) {
            getSupportFragmentManager().beginTransaction()
                    .remove(mapFragment)
                    .add(R.id.mapContainer, MapFragment.newInstance(Tags, currentLocation), "mapfragment")
                    .commit();
        } else {
            mapFragment = MapFragment.newInstance(Tags, currentLocation);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mapContainer, mapFragment, "mapfragment")
                    .commit();
        }

        // Get currentUser's last known location while waiting for OnLocationChanged
        getLastKnownLocation();

        locationManager = getSystemService(LocationManager.class);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    private void displayUserInfo() { //Display current username to the app bar -- Welcome, user123
        firestore = FirebaseFirestore.getInstance();
        firestore.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getData().get("id").equals(firebaseUser.getUid())) {
                                    txt_username = (String) document.getData().get("username");
                                    txt_email = (String) document.getData().get("email");
                                    getSupportActionBar().setTitle("Welcome, " + txt_username);
                                    break;
                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            Toast.makeText(HomeActivity.this, "Error getting user from Firestore to display in action bar..", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void startCreateTagActivity() {
        createTagIntent = new Intent(HomeActivity.this, CreateTagActivity.class);
        startActivity(createTagIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            displayUserInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation();
            showLocationUpdates();
        }
    }

    private void getLastKnownLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            currentLocation = new Location(location);
                            fetchTags(); //after getting last known location, start fetching tags from Firestore
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    // Already checking necessary permission before calling requestLocationUpdates
    private void showLocationUpdates() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, this.locationListener); // GPS
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, this.locationListener); // Cell sites
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 10, this.locationListener); // WiFi
    }

    private void fetchTags(){
        firestore = FirebaseFirestore.getInstance();
        firestore.collection("Tags").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) { //addSnapshotListener will listen to data changes from Firestore and be triggered if there are changes made
                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (documentChange.getType()) {
                        case ADDED:
                            double mTagLocationLatitude = (Double) documentChange.getDocument().getData().get("locationLat");
                            double mTagLocationLongitude = (Double) documentChange.getDocument().getData().get("locationLong");
                            if (distanceCalculator.distanceFromTo(mTagLocationLatitude, mTagLocationLongitude, currentLocation.getLatitude(), currentLocation.getLongitude()) <= MAX_RADIUS) {
                                String mTagID = documentChange.getDocument().getData().get("id").toString();
                                String mTagDuration = documentChange.getDocument().getData().get("duration").toString();
                                String mTagImage = documentChange.getDocument().getData().get("imageRef").toString();
                                String mTagDescription = documentChange.getDocument().getData().get("description").toString();
                                String mTagLocationName = documentChange.getDocument().getData().get("locationName").toString();
                                int mTagUpvoteCount = Integer.valueOf(documentChange.getDocument().getData().get("upvoteCount").toString());
                                int mTagDownvoteCount = Integer.valueOf(documentChange.getDocument().getData().get("downvoteCount").toString());
                                int mTagPopularity = Integer.valueOf(documentChange.getDocument().getData().get("popularityCount").toString());
                                String mTagCreatedBy = documentChange.getDocument().getData().get("createdBy").toString();
                                String mTagCreatedById = documentChange.getDocument().getData().get("createdById").toString();
                                Tag tag = new Tag(mTagID, mTagLocationName, mTagDuration, mTagImage, mTagDescription, mTagLocationLatitude,
                                        mTagLocationLongitude, mTagUpvoteCount, mTagDownvoteCount, mTagPopularity, mTagCreatedBy, mTagCreatedById);
                                Tags.add(tag);
                            }
                        case REMOVED:
                            //Do something
                        case MODIFIED:
                            //Do something
                    }
                }

                //Create a tag recycler list fragment after fetching tags
                tagRecyclerViewFragment = (TagRecyclerViewFragment) getSupportFragmentManager().findFragmentByTag(TAG_LIST_FRAGMENT);
                if (null != tagRecyclerViewFragment) {
                    tagRecyclerViewFragment.updateDataSet(Tags);
                } else {
                    tagRecyclerViewFragment = TagRecyclerViewFragment.newInstance(Tags);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.tag_recycler_fragment_container, tagRecyclerViewFragment, TAG_LIST_FRAGMENT)
                            .commitAllowingStateLoss();
                }

                mapFragment.updateNewTagsLocations(Tags, currentLocation);
            }
        });
    }
}
