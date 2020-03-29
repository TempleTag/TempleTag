package edu.temple.templetag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class CreateTagActivity extends AppCompatActivity {
    // New Tag
    private Tag newTag;
    private int mTagID; // need to generate a unique tag ID for each tag
    private int mTagDuration; // something we set that's fixed
    private String mTagImage; // user has option to take a picture to represent the tag, otherwise we show no image (null)
    private String mTagDescription; // user sets when creating describing what's in the tag
    private String mTagLocationName; // user sets when creating, naming the tag location
    private double mTagLocationLatitude; // we set based off user's current location
    private double mTagLocationLongitude; // we set based off user's current location
    private int mTagUpvoteCount; // changes as other users upvote
    private int mTagDownvoteCount; // changes as other users downvote
    private int mTagPopularity; // if multiple users tag the same location, we increase the marker size on the map using this field
    private String mTagCreatedBy; // name of user that created the tag

    // Firebase
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Views
    private MaterialEditText tagLocationNameInput;
    private MaterialEditText tagDescriptionInput;
    private ImageView mTagImageView = null;
    private Button takeTagPictureBtn;
    private Button createTagBtn;

    private static final String TAG = "CreateTagActivity";

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is authenticated, otherwise redirect to LoginActivity
        firebaseUser = firebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null){
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

        takeTagPictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Android Camera API work here
                // Use Picasso to load the picture Bitmap taken into the mTagImageView
            }
        });

        createTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create new tag in database
                createInDatabase();
            }
        });

    }

    private boolean createInDatabase() {
        final boolean[] createdInDatabase = {false};

        createTag(); // builds newTag

        HashMap<String, Object> newTagMap = new HashMap<>();
        newTagMap.put("id", newTag.getmTagID());
        newTagMap.put("location_name", newTag.getmTagLocationName());
        newTagMap.put("description", newTag.getmTagDescription());
        newTagMap.put("created_by", newTag.getmTagCreatedBy());
        newTagMap.put("image_ref", newTag.getmTagImageURI());
        newTagMap.put("location_lat", newTag.getmTagLocationLat());
        newTagMap.put("location_long", newTag.getmTagLocationLong());
        newTagMap.put("popularityCount", newTag.getmTagPopularity());
        newTagMap.put("upvoteCount", newTag.getmTagUpvoteCount());
        newTagMap.put("downvoteCount", newTag.getmTagDownvoteCount());
        newTagMap.put("duration", newTag.getmTagDuration());

        // Save newTag to Firestore
        firestore.collection("Users")
                .add(newTagMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        createdInDatabase[0] = true;
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
        mTagID = Integer.parseInt(UUID.randomUUID().toString());
        mTagDuration = 0; // TODO will need to use Date objects to generate a duration to check later to determine whether to delete tag or not
        mTagImage = "tag-image-storage-reference in FirebaseStorage use to retrieve to display in TagRecyclerViewFragment and anywhere else a tag's image needs to be shown" ; // TODO will need to figure out saving image to Firebase Storage and holding reference to where that image is stored here
        mTagLocationName = Objects.requireNonNull(tagLocationNameInput.getText()).toString();
        mTagDescription = Objects.requireNonNull(tagDescriptionInput.getText()).toString();
        // mTagLocationLatitude = // TODO get current location information using LocationManager
        // mTagLocationLongitude =
        mTagUpvoteCount = 0;
        mTagDownvoteCount = 0;
        mTagCreatedBy = firebaseUser.getDisplayName();

        if (checkCollation()) {
            // Tags at same location name exist
            newTag = new Tag(mTagID, mTagLocationName, mTagDuration, mTagImage, mTagDescription, mTagLocationLatitude,
                    mTagLocationLongitude, mTagUpvoteCount, mTagDownvoteCount, mTagPopularity, mTagCreatedBy);
        } else {
            // A tag at this location name does not exist
            newTag = new Tag(mTagID, mTagLocationName, mTagDuration, mTagImage, mTagDescription, mTagLocationLatitude,
                    mTagLocationLongitude, mTagUpvoteCount, mTagDownvoteCount, mTagPopularity, mTagCreatedBy);
        }
    }

    // Checks if other tags already exist at a certain location name, increasing the newTag's popularity and in turn, it's map marker size
    private boolean checkCollation() {
        boolean popularTag = false;
        // Query using get() on firestore, increment private field mTagPopularity for each one that matches in DB
        firestore.collection("Tags")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                // TODO Search firestore database for if other tag exists from same user set location name, if so increment our private field mTagPopularity that will be used later to create a new tag
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            // TODO adjust toast message to be user friendly
                            Toast.makeText(CreateTagActivity.this, "Error fetching tags from database to check collation..", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        // returns popularTag if there are other tags with the same location name
        return true;
        // returns false if there are no tags in the database with the same location name
    }
}
