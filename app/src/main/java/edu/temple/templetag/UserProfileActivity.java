package edu.temple.templetag;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import edu.temple.templetag.fragments.TagRecyclerViewFragment;

public class UserProfileActivity extends AppCompatActivity {

    private String txt_username, txt_email;
    private TextView change_username_textview, username_textview, email_textview, tagcount_textview;
    private String dialogText;
    final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private ArrayList<Tag> Tags = new ArrayList<>();
    public static final String TAG_LIST_FRAGMENT = "TagRecyclerFragment_USER";
    TagRecyclerViewFragment tagRecyclerViewFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        //Display greeting + username
        displayInfo();
        //Init textviews and buttons
        tagcount_textview = findViewById(R.id.tagCount);

        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
                finish();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Account Setting");
        }
        //Change Username
        change_username_textview = findViewById(R.id.change_username);
        change_username_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickChangeCurrentUsername();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeUsername(){
        //Go through firebase and change username
        firestore.collection("Users")
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                boolean canChangeUsername = true;
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if ((!document.getData().get("id").equals(firebaseUser.getUid())) && document.getData().get("username").equals(dialogText)) {
                            canChangeUsername = false;
                        }
                    }
                    if (canChangeUsername) {
                        DocumentReference reference = firestore.collection("Users").document(firebaseUser.getUid());
                        reference.update("username", dialogText).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                username_textview.setText("Hi, " + dialogText);
                            }
                        });
                    } else {
                        Toast.makeText(UserProfileActivity.this, "You cannot use this username", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void displayInfo(){
        txt_username = getIntent().getExtras().getString("username");
        txt_email = getIntent().getExtras().getString("email");
        username_textview = findViewById(R.id.username);
        username_textview.setText("Hi, " + txt_username);
        email_textview = findViewById(R.id.emailTextView);
        email_textview.setText(txt_email);
        tagcount_textview = findViewById(R.id.tagCount);
    }

    private void displayMyTagListFragment() {
        new Thread() {
            public void run() {
                firestore.collection("Tags")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Tags.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                if (document.getData().get("createdById").equals(firebaseUser.getUid())) {
                                    double mTagLocationLatitude = (Double) document.getData().get("locationLat"); // we set based off user's current location
                                    double mTagLocationLongitude = (Double) document.getData().get("locationLong"); // we set based off user's current location
                                    String mTagID = document.getData().get("id").toString(); // need to generate a unique tag ID for each tag
                                    String mTagDuration = document.getData().get("duration").toString(); // current Date tag was created, used to compare two dates later to determine which Tags get deleted in the database and which don't
                                    String mTagImage = document.getData().get("imageRef").toString(); // user has option to take a picture to represent the tag, otherwise we show no image (null)
                                    String mTagDescription = document.getData().get("description").toString(); // user sets when creating describing what's in the tag
                                    String mTagLocationName = document.getData().get("locationName").toString(); // user sets when creating, naming the tag location
                                    int mTagUpvoteCount = Integer.valueOf(document.getData().get("upvoteCount").toString()); // changes as other users upvote
                                    int mTagDownvoteCount = Integer.valueOf(document.getData().get("downvoteCount").toString()); // changes as other users downvote
                                    int mTagPopularity = Integer.valueOf(document.getData().get("popularityCount").toString()); // if multiple users tag the same location, we increase the marker size on the map using this field
                                    String mTagCreatedBy = document.getData().get("createdBy").toString(); // name of user that created the tag
                                    String mTagCreatedById = document.getData().get("createdById").toString();

                                    Tag tag = new Tag(mTagID, mTagLocationName, mTagDuration, mTagImage, mTagDescription, mTagLocationLatitude,
                                    mTagLocationLongitude, mTagUpvoteCount, mTagDownvoteCount, mTagPopularity, mTagCreatedBy, mTagCreatedById);
                                    Tags.add(tag);
                                }
                            }
                            // Update active tags
                            if (Tags.size() == 1) {
                                tagcount_textview.setText(Tags.size() + " active tag");
                            } else {
                                tagcount_textview.setText(Tags.size() + " active tags");
                            }

                            // Create tag recycler list fragment after fetching tags
                            tagRecyclerViewFragment = (TagRecyclerViewFragment) getSupportFragmentManager().findFragmentByTag(TAG_LIST_FRAGMENT);
                            BlankFragment blankFragment = (BlankFragment)getSupportFragmentManager().findFragmentByTag("blankfragment");

                            if (null != tagRecyclerViewFragment) {
                                tagRecyclerViewFragment.updateDataSet(Tags);
                            } else {
                                tagRecyclerViewFragment = TagRecyclerViewFragment.newInstance(Tags);
                                getSupportFragmentManager().beginTransaction()
                                        .add(R.id.tag_recycler_fragment_container, tagRecyclerViewFragment, TAG_LIST_FRAGMENT)
                                        .commit();
                            }

                            if (Tags.size() > 0) {
                                if (blankFragment != null){
                                    getSupportFragmentManager().beginTransaction()
                                            .remove(blankFragment)
                                            .commitAllowingStateLoss();
                                }
                            } else {
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.tag_recycler_fragment_container, BlankFragment.newInstance("You don't have any active tags"), "blankfragment")
                                        .commitAllowingStateLoss();
                            }
                        } else {
                            Toast.makeText(UserProfileActivity.this, "Error getting Tags", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }.start();
    }

    private void OnClickChangeCurrentUsername(){
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("New Username");

        // Set up the input
        final EditText input = new EditText(UserProfileActivity.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogText = input.getText().toString();
                changeUsername();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayMyTagListFragment();
    }
}
