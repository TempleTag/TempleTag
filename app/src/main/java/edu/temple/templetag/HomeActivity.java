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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.temple.templetag.fragments.MapFragment;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private FloatingActionButton createTagBtn;
    private Intent createTagIntent;
    private String txt_username, txt_email;
    //Location
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FusedLocationProviderClient fusedLocationClient;
    //Current User
    public static Location currentLocation;
    private MapFragment mapFragment;
    //Timer for periodic tag fetch from firestore
    Timer timerRef;
    //Tags
    private ArrayList<Tag> Tags = new ArrayList<>();

    public static final String TAG = "HomeActivity";
    public static final int RELOAD_TIME = 10000;

    @Override
    protected void onStart() {
        super.onStart();

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

        // Attach mapFragment
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragment");
        if (null != mapFragment) {
            getSupportFragmentManager().beginTransaction()
                    .remove(mapFragment)
                    .add(R.id.mapContainer, MapFragment.newInstance(), "mapfragment")
                    .commit();
        } else {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mapContainer, mapFragment, "mapfragment")
                    .commit();
        }

        //Profile On CLick
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

        //Check permission and display tags
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }

        // Get currentUser's location using device permission
        getLastKnownLocation();
        locationManager = getSystemService(LocationManager.class);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation.setLatitude(location.getLatitude());
                currentLocation.setLongitude(location.getLongitude());
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

    private void displayUserInfo() {
        firestore = FirebaseFirestore.getInstance();

        firestore.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getData().get("id").equals(firebaseUser.getUid())) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
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

        //fetch Tags
        if (timerRef == null) {
            Timer timer = new Timer();
            timerRef = timer;
            timer.schedule(new fetchTag(), 0, RELOAD_TIME);
        }

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

    class fetchTag extends TimerTask {
        public void run() {
            new Thread() {
                public void run() {
                    firestore = FirebaseFirestore.getInstance();
                    firestore.collection("Tags").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String mTagID = document.getData().get("id").toString(); // need to generate a unique tag ID for each tag
                                    String mTagDuration = document.getData().get("duration").toString(); // current Date tag was created, used to compare two dates later to determine which Tags get deleted in the database and which don't
                                    String mTagImage = document.getData().get("imageRef").toString(); // user has option to take a picture to represent the tag, otherwise we show no image (null)
                                    String mTagDescription = document.getData().get("description").toString(); // user sets when creating describing what's in the tag
                                    String mTagLocationName = document.getData().get("locationName").toString(); // user sets when creating, naming the tag location
                                    double mTagLocationLatitude = (Double) document.getData().get("locationLat"); // we set based off user's current location
                                    double mTagLocationLongitude = (Double) document.getData().get("locationLong"); // we set based off user's current location
                                    int mTagUpvoteCount = Integer.valueOf(document.getData().get("upvoteCount").toString()); // changes as other users upvote
                                    int mTagDownvoteCount = Integer.valueOf(document.getData().get("downvoteCount").toString()); // changes as other users downvote
                                    int mTagPopularity = Integer.valueOf(document.getData().get("popularityCount").toString()); // if multiple users tag the same location, we increase the marker size on the map using this field
                                    String mTagCreatedBy = document.getData().get("createdBy").toString(); // name of user that created the tag

                                    Tag tag = new Tag(mTagID, mTagLocationName, mTagDuration, mTagImage, mTagDescription, mTagLocationLatitude,
                                            mTagLocationLongitude, mTagUpvoteCount, mTagDownvoteCount, mTagPopularity, mTagCreatedBy);
                                    Tags.add(tag);
                                    Log.d("tag", tag.getmTagLocationName().toString());
                                }
                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                                Toast.makeText(HomeActivity.this, "Error getting Tags", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    runOnUiThread(new Thread(new Runnable() {
                        public void run() {
                            mapFragment.displayMarkers(Tags, currentLocation);
                        }
                    }));
                }
            }.start();
        }
    }
}
