package edu.temple.templetag;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rengwuxian.materialedittext.MaterialEditText;

public class CreateTagActivity extends AppCompatActivity {
    // New Tag
    private Tag newTag;
    private int mTagID; // need to generate a unique tag ID for each tag
    private int mTagDuration; // something we set that's fixed
    private Bitmap mTagImage; // user has option to take a picture to represent the tag, otherwise we show no image (null)
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
    private FirebaseFirestore firestore;

    // Views
    private MaterialEditText tagLocationNameInput;
    private MaterialEditText tagDescriptionInput;
    private ImageView mTagImageView;

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


    }

    private boolean createInDatabase() {
        // TODO call createTag()
        // returns true if Tag was created in database successfully
        return true;
    }

    private void createTag() {
        // TODO call checkCollation() to increase newTag's mTagPopularity field if there are more tags at the same location already
    }

    private boolean checkCollation() {
        // TODO Search firestore database for if other tag exists from same user set location name, if so increment it's popularity
        // returns true if there are other tags with the same location name
        return true;
    }
}
