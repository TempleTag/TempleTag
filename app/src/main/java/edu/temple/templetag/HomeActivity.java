package edu.temple.templetag;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.temple.templetag.fragments.MapFragment;
import edu.temple.templetag.fragments.TagRecyclerViewFragment;
import edu.temple.templetag.tools.DistanceCalculator;

public class HomeActivity extends AppCompatActivity {
    //Firebase and Firestore
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    //Views
    private FloatingActionButton createTagBtn;

    //Location
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FusedLocationProviderClient fusedLocationClient;
    public static Location currentLocation;

    // Fragments
    private MapFragment mapFragment;
    TagRecyclerViewFragment tagRecyclerViewFragment;

    // Tags
    private ArrayList<Tag> Tags = new ArrayList<>();

    // Distance Calculator
    DistanceCalculator distanceCalculator = new DistanceCalculator();

    private boolean firstStart = true;

    //Other variables
    private Intent createTagIntent;
    private String txt_username, txt_email;
    public static final String TAG = "HomeActivity";
    public static final String TAG_LIST_FRAGMENT = "TagRecyclerFragment_HOME"; //This is the tag for TagRecyclerViewFragment for HomeActivity. This is not to be confused with the one in UserProfileActivity
    private final String MAP_FRAG_IN_HOME = "MapFragmentInHomeActivity"; //MapFragment Tag to distinguish with MapFragment in TagDetailActivity
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

        if (!firstStart) {
            try {
                fetchTags();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //Change StatusBar Color
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.TempleTagTheme));

        createTagBtn = findViewById(R.id.createTagBtn);

        createTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateTagActivity();
            }
        });

        // Profile Icon On CLick
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

        locationManager = getSystemService(LocationManager.class);
        locationListener = new LocationListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onLocationChanged(Location location) {
                // Fetch new tags for currentLocation
                currentLocation = location;
                try {
                    fetchTags();
                    firstStart = false;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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

        // Check location permission
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        } else {
            showLocationUpdates();
        }

        // Attach mapFragment
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MAP_FRAG_IN_HOME);
        if (mapFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(mapFragment)
                    .add(R.id.mapContainer, MapFragment.newInstance(Tags, currentLocation), MAP_FRAG_IN_HOME)
                    .commit();
        } else {
            mapFragment = MapFragment.newInstance(Tags, currentLocation);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mapContainer, mapFragment, MAP_FRAG_IN_HOME)
                    .commit();
        }
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
            showLocationUpdates();
        }
    }

    // Already checking necessary permission before calling requestLocationUpdates
    @SuppressLint("MissingPermission")
    private void showLocationUpdates() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, this.locationListener); // GPS
    }

    private void fetchTags() throws ParseException {
        // Get current to compare
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);
        SimpleDateFormat df = new SimpleDateFormat("MMM-dd-yyyy");
        String formattedDate = df.format(c);
        final Date currentDate = new SimpleDateFormat("MMM-dd-yyyy").parse(formattedDate);
        Log.d(TAG, "Current Date: " + currentDate.toString());


        firestore = FirebaseFirestore.getInstance();
        Tags.clear(); // clear the current tags - if fetchTags is being called again it is because of a location change and we only want to show the tags closest to the user's current location
        firestore.collection("Tags").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) { // addSnapshotListener will listen to data changes from Firestore and be triggered if there are changes made
                for (final DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                    Log.d(TAG, "Tag document: " + documentChange.toString());
                    switch (documentChange.getType()) {
                        case ADDED:

                            // get tag downvoteCount
                            int parsedDownvoteCount = Integer.parseInt(documentChange.getDocument().getData().get("downvoteCount").toString());

                            // Check if tag is older than a day, if it is, delete that tag and the image in FirebaseStorage for that tag
                            String tagDateString = (documentChange.getDocument().getData().get("duration").toString());
                            String tagImageUrl = documentChange.getDocument().getData().get("imageRef").toString();
                            Date tagDate = null;
                            try {
                                tagDate = new SimpleDateFormat("MMM-dd-yyyy").parse(tagDateString);
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }

                            long differenceInTime = currentDate.getTime() - tagDate.getTime();
                            long differenceInDays = differenceInTime / (1000 * 3600 * 24);
                            Log.d(TAG, "Tag difference in days: " + differenceInDays);
                            Log.d(TAG, "Tag image url: " + tagImageUrl);

                            if ((differenceInDays > 0) || (parsedDownvoteCount > 5)) {
                                // Delete expired tag from Firestore and its image from FirebaseStorage
                                // 1. Delete expired tag's image from FirebaseStorage
                                if (tagImageUrl.contains("https://firebasestorage.googleapis.com")) {
                                    StorageReference expiredTagImageRef = firebaseStorage.getReferenceFromUrl(tagImageUrl);
                                    expiredTagImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: Expired tag's image deleted from FirebaseStorage");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: There was an error deleting an expired tag's image from FirebaseStorage: " + e);
                                        }
                                    });
                                }
                                // 2. Delete expired tag from Firestore
                                firestore.collection("Tags")
                                        .document(documentChange.getDocument().getId())
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: Deleted tag: " + documentChange.getDocument().getData().get("locationName"));
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: Error deleting tag: " + documentChange.getDocument().getData().get("locationName") + " Error: " + e);
                                            }
                                        });
                            } else {
                                // Create tags to display on Map depending on user's current location
                                double mTagLocationLatitude = (Double) documentChange.getDocument().getData().get("locationLat");
                                double mTagLocationLongitude = (Double) documentChange.getDocument().getData().get("locationLong");
                                if (distanceCalculator.distanceFromTo(mTagLocationLatitude, mTagLocationLongitude, currentLocation.getLatitude(), currentLocation.getLongitude()) <= MAX_RADIUS) {
                                    String mTagID = documentChange.getDocument().getData().get("id").toString();
                                    String mTagDuration = documentChange.getDocument().getData().get("duration").toString();
                                    String mTagImage = documentChange.getDocument().getData().get("imageRef").toString();
                                    String mTagDescription = documentChange.getDocument().getData().get("description").toString();
                                    String mTagLocationName = documentChange.getDocument().getData().get("locationName").toString();
                                    int mTagUpvoteCount = Integer.parseInt(documentChange.getDocument().getData().get("upvoteCount").toString());
                                    int mTagDownvoteCount = Integer.parseInt(documentChange.getDocument().getData().get("downvoteCount").toString());
                                    int mTagPopularity = Integer.parseInt(documentChange.getDocument().getData().get("popularityCount").toString());
                                    String mTagCreatedBy = documentChange.getDocument().getData().get("createdBy").toString();
                                    String mTagCreatedById = null;
                                    if (documentChange.getDocument().getData().get("createdById") != null) {
                                        mTagCreatedById = documentChange.getDocument().getData().get("createdById").toString();
                                    }
                                    Tag tag = new Tag(mTagID, mTagLocationName, mTagDuration, mTagImage, mTagDescription, mTagLocationLatitude,
                                            mTagLocationLongitude, mTagUpvoteCount, mTagDownvoteCount, mTagPopularity, mTagCreatedBy, mTagCreatedById);
                                    Tags.add(tag);
                                }
                            }
                        case REMOVED:
                            // Do something
                        case MODIFIED:
                            // Do something
                    }
                }

                // Create a TagRecyclerViewFragment after fetching new tags near user's current location
                tagRecyclerViewFragment = (TagRecyclerViewFragment) getSupportFragmentManager().findFragmentByTag(TAG_LIST_FRAGMENT);
                BlankFragment blankFragment = (BlankFragment)getSupportFragmentManager().findFragmentByTag("blankfragment");

                if (tagRecyclerViewFragment != null) {
                    tagRecyclerViewFragment.updateDataSet(Tags);
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.tag_recycler_fragment_container, TagRecyclerViewFragment.newInstance(Tags), TAG_LIST_FRAGMENT)
                            .commitAllowingStateLoss();
                }

                if (Tags.size() > 0) {
                    if (blankFragment != null){
                        getSupportFragmentManager().beginTransaction()
                                .remove(blankFragment)
                                .commitAllowingStateLoss();
                    }
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.tag_recycler_fragment_container, BlankFragment.newInstance("Opps! There are no nearby events"), "blankfragment")
                            .commitAllowingStateLoss();
                }

                try {
                    mapFragment.updateNewTagsLocations(Tags, currentLocation);
                } catch (IllegalStateException er){
                    er.printStackTrace();
                }
            }
        });
    }
}
