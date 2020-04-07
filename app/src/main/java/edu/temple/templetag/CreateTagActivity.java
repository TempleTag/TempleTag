package edu.temple.templetag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class CreateTagActivity extends AppCompatActivity {
    // New Tag
    private Tag newTag;
    private String mTagID; // need to generate a unique tag ID for each tag
    private String mTagDuration; // current Date tag was created, used to compare two dates later to determine which Tags get deleted in the database and which don't
    private String mTagImage; // user has option to take a picture to represent the tag, otherwise we show no image (null)
    private String mTagDescription; // user sets when creating describing what's in the tag
    private String mTagLocationName; // user sets when creating, naming the tag location
    private double mTagLocationLatitude; // we set based off user's current location
    private double mTagLocationLongitude; // we set based off user's current location
    private int mTagUpvoteCount; // changes as other users upvote
    private int mTagDownvoteCount; // changes as other users downvote
    private int mTagPopularity = 0; // if multiple users tag the same location, we increase the marker size on the map using this field
    private String mTagCreatedBy; // name of user that created the tag
    private String mTagCreatedById;

    // Firebase
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Location
    LocationManager locationManager;
    LocationListener locationListener;

    // Views
    private MaterialEditText tagLocationNameInput;
    private MaterialEditText tagDescriptionInput;
    private ImageView mTagImageView = null;
    private ImageButton takeTagPictureBtn;
    private Button createTagBtn;
    private TextView expiration_text_view;

    //Date
    Date c = Calendar.getInstance().getTime();
    SimpleDateFormat df = new SimpleDateFormat("MMM-dd-yyyy");

    private static final String TAG = "CreateTagActivity";

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is authenticated, otherwise redirect to LoginActivity
        if (firebaseUser == null) {
            Intent loginIntent = new Intent(CreateTagActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tag);

        // Get reference to all views
        tagLocationNameInput = findViewById(R.id.tagLocationName);
        tagDescriptionInput = findViewById(R.id.tagDescription);
        mTagImageView = findViewById(R.id.tagImage);
        takeTagPictureBtn = findViewById(R.id.takeTagPictureBtn);
        createTagBtn = findViewById(R.id.createTagBtn);
        expiration_text_view = findViewById(R.id.expiration_text_view);

        // Get currentUser's location using device permission
        locationManager = getSystemService(LocationManager.class);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mTagLocationLatitude = location.getLatitude();
                mTagLocationLongitude = location.getLongitude();
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

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        } else {
            showLocationUpdates();
        }

        takeTagPictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Handle Android Camera API work here
                // TODO Use Picasso to load the picture Bitmap taken into the mTagImageView
                Toast.makeText(CreateTagActivity.this, "Implement camera API and Firebase Storage to handle this feature.", Toast.LENGTH_LONG).show();
            }
        });

        createTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create new tag in database
                createInDatabase();
            }
        });

        //Inform expiration date to user
        expiration_text_view.setText("This tag will expired on "+df.format(c));
    }

    private boolean createInDatabase() {
        final boolean[] createdInDatabase = {false};

        createTag(); // builds newTag

        HashMap<String, Object> newTagMap = new HashMap<>();
        newTagMap.put("id", newTag.getmTagID());
        newTagMap.put("locationName", newTag.getmTagLocationName());
        newTagMap.put("description", newTag.getmTagDescription());
        newTagMap.put("createdBy", newTag.getmTagCreatedBy());
        newTagMap.put("imageRef", newTag.getmTagImageURI());
        newTagMap.put("locationLat", newTag.getmTagLocationLat());
        newTagMap.put("locationLong", newTag.getmTagLocationLong());
        newTagMap.put("popularityCount", newTag.getmTagPopularity());
        newTagMap.put("upvoteCount", newTag.getmTagUpvoteCount());
        newTagMap.put("downvoteCount", newTag.getmTagDownvoteCount());
        newTagMap.put("duration", newTag.getmTagDuration());
        newTagMap.put("createdById", newTag.getmTagCreatedById());

        // Save newTag to Firestore
        firestore.collection("Tags")
                .add(newTagMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        createdInDatabase[0] = true;
                        Toast.makeText(CreateTagActivity.this, "Created your Tag!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateTagActivity.this, "Something went wrong! Please try again.", Toast.LENGTH_LONG).show();
                        createdInDatabase[0] = false;
                    }
                });

        return createdInDatabase[0];
    }

    private void createTag() {
        // Get date tag was created

        String formattedDate = df.format(c);

        mTagID = UUID.randomUUID().toString();
        mTagDuration = formattedDate;
        mTagImage = "tag-image-storage-reference" ; // TODO Figure out saving an image to Firebase Storage and holding reference to where that image is stored in Firestore
        mTagLocationName = Objects.requireNonNull(tagLocationNameInput.getText()).toString();
        mTagDescription = Objects.requireNonNull(tagDescriptionInput.getText()).toString();
        mTagUpvoteCount = 0;
        mTagDownvoteCount = 0;
        mTagCreatedBy = firebaseUser.getDisplayName();
        mTagCreatedById = firebaseUser.getUid();

        if (checkCollation()) {
            // Tags at same location name exist
            newTag = new Tag(mTagID, mTagLocationName, mTagDuration, mTagImage, mTagDescription, mTagLocationLatitude,
                    mTagLocationLongitude, mTagUpvoteCount, mTagDownvoteCount, mTagPopularity, mTagCreatedBy, mTagCreatedById);
        } else {
            // A tag at this location name does not exist
            newTag = new Tag(mTagID, mTagLocationName, mTagDuration, mTagImage, mTagDescription, mTagLocationLatitude,
                    mTagLocationLongitude, mTagUpvoteCount, mTagDownvoteCount, mTagPopularity, mTagCreatedBy, mTagCreatedById);
        }
    }

    // Checks if other tags already exist at a certain location name, which increases the newTag's popularity and updates the popularityCount for all other tags at the same location
    // The popularityCount field will the tag's map marker size when we pull in tags from the Firestore database and pass them to the MapFragment
    private boolean checkCollation() {
        final boolean[] popularTag = {false};
        // Query Firestore - increment private field mTagPopularity for each tag that matches the new tag's location name in Firestore database
        firestore.collection("Tags")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getData().get("locationName").equals(mTagLocationName)) {
                                    Log.d(TAG, "onComplete: " + document.getData().get("locationName") + " was equal to new tag's location: " + mTagLocationName);
                                    popularTag[0] = true;
                                    mTagPopularity += 1; // if there were other tags in the database with the same locationName, increment tag's popularity rank
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents but task completed", task.getException());
                            Toast.makeText(CreateTagActivity.this, "There was an error getting documents to compare", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to get data from Firestore database..", e);
                        Toast.makeText(CreateTagActivity.this, "Failed to get data from Firestore database..", Toast.LENGTH_LONG).show();
                    }
                });

        // Query Firestore - update each tag that was equal to new tag's location's popularity to have same popularityCount
        firestore.collection("Tags")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getData().get("locationName").equals(mTagLocationName)) {
                                    // Update specific tags with same location name as new tag's location popularity ranking
                                    firestore.collection("Tags").document(document.getId()).update("popularityCount", mTagPopularity);
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents but task completed", task.getException());
                            Toast.makeText(CreateTagActivity.this, "There was an error getting documents to compare", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to get data from Firestore database..", e);
                        Toast.makeText(CreateTagActivity.this, "Failed to get data from Firestore database..", Toast.LENGTH_LONG).show();
                    }
                });

        return popularTag[0];
    }

    // Location methods and necessary lifecycle callbacks
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showLocationUpdates();
        }
    }

    @SuppressLint("MissingPermission") // Already checking necessary permission before calling requestLocationUpdates
    private void showLocationUpdates() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, this.locationListener); // GPS
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, this.locationListener); // Cell sites
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 10, this.locationListener); // WiFi
    }
}
