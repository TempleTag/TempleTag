package edu.temple.templetag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.location.Location;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
<<<<<<<<< Temporary merge branch 1
=========
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
>>>>>>>>> Temporary merge branch 2
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
<<<<<<<<< Temporary merge branch 1
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
=========
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
>>>>>>>>> Temporary merge branch 2
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.temple.templetag.adapters.TagRecyclerViewAdapter;
import edu.temple.templetag.fragments.MapFragment;

public class TagDetailActivity extends AppCompatActivity {

    TextView tagLocationName, tagCreatedBy, tagUpVote, tagDownVote, tagPop, tagDesc;
    CircleImageView upvoteIcon, downvoteIcon;
    public FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    ImageButton delBtn;
    ImageView tagImageView;
    MapFragment mapFragment;
    public static final String TAG = "TagDetailActivity";



    private final String MAP_FRAG_IN_DETAIL = "MapFragmentInDetailActivity"; //MapFragment Tag to distinguish with MapFragment in HomeActivity
    Tag mTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_detail);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        mTag = getIntent().getExtras().getParcelable(TagRecyclerViewAdapter.SELECTED_TAG);
        tagLocationName = findViewById(R.id.tag_location_name);
        tagCreatedBy = findViewById(R.id.tag_created_by);
        tagUpVote = findViewById(R.id.tag_up_vote);
        tagDownVote = findViewById(R.id.tag_down_vote);
        tagPop = findViewById(R.id.tag_popularity);
        tagDesc = findViewById(R.id.tag_description);
        tagImageView = findViewById(R.id.tag_image_view);

        tagLocationName.setText(mTag.getmTagLocationName());
        tagCreatedBy.setText("created by " + mTag.getmTagCreatedBy());
        tagUpVote.setText(mTag.getmTagUpvoteCount() + " Upvotes");
        tagDownVote.setText(mTag.getmTagDownvoteCount() + " Downvotes");
        if (mTag.getmTagPopularity() == 1) {
            tagPop.setText(mTag.getmTagPopularity() + " person is talking about this event");
        } else {
            tagPop.setText(mTag.getmTagPopularity() + " people are talking about this event");
        }
        tagDesc.setText(mTag.getmTagDescription());
        tagDesc.setMovementMethod(new ScrollingMovementMethod());

        delBtn = findViewById(R.id.btn_delete);
        if (mTag.getmTagCreatedById() != null) {
            if (!mTag.getmTagCreatedById().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){ //Show delete button if the tag belows to the current user, hide it otherwise
                delBtn.setVisibility(View.INVISIBLE);
            } else {
                delBtn.setVisibility(View.VISIBLE);
            }
        }
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String LOG_OUT = "TagDetailActivity";

                // button should not be clickable by a different user but just to be safe
                if (mTag.getmTagCreatedById().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                    if (mTag.getmTagImageURI().contains("https://firebasestorage.googleapis.com")) {
                        StorageReference expiredTagImageRef = firebaseStorage.getReferenceFromUrl(mTag.getmTagImageURI());
                        expiredTagImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(LOG_OUT, "onSuccess: Tag's image deleted from FirebaseStorage");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(LOG_OUT, "onFailure: There was an error deleting the tag's image from FirebaseStorage: " + e);
                            }
                        });
                    }

                    firestore.collection("Tags")
                            .document(mTag.getmTagID())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(LOG_OUT, "onSuccess: Deleted tag: " + mTag.getmTagLocationName());
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(LOG_OUT, "onFailure: Error deleting tag: " + mTag.getmTagLocationName() + " Error: " + e);
                                }
                            });
                }
            }
        });


        // Code for tag upvotes and downvotes
        upvoteIcon = findViewById(R.id.up_vote_icon);
        downvoteIcon = findViewById(R.id.down_vote_icon);
        firestore = FirebaseFirestore.getInstance();

        final DocumentReference tagRef = firestore
                .collection("Tags")
                .document(mTag.getmTagID());

        upvoteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Does an upvote through a Firestore Transaction which prevents race conditions
                firestore.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(tagRef);

                        int newVotes = (int)(snapshot.getDouble("upvoteCount") + 1);
                        transaction.update(tagRef, "upvoteCount", newVotes);

                        // Success
                        return null;
                    }
                })                    //Testing for success or failure
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Tag Voted Up Transaction success!");
                        Toast.makeText(getApplicationContext(), "Tag Voted Up", Toast.LENGTH_SHORT).show();

                        //Updates local upvote number with a +1
                        tagUpVote.setText(mTag.getmTagUpvoteCount()+1 + " Upvotes");
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Tag Voted Up Transaction failure.", e);
                                Toast.makeText(getApplicationContext(), "Up Vote Failed", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        //Does an downvote through a Firestore Transaction which prevents race conditions
        downvoteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firestore.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(tagRef);
                        // Note: this could be done without a transaction
                        //       by updating the population using FieldValue.increment()
                        int newVotes = (int)(snapshot.getDouble("downvoteCount") + 1);
                        transaction.update(tagRef, "downvoteCount", newVotes);

                        // Success
                        return null;
                    }
                })                    //Testing for success or failure
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Tag Voted Down Transaction success!");
                        Toast.makeText(getApplicationContext(), "Tag Voted Down", Toast.LENGTH_SHORT).show();
                        //Updates local downvote number with a +1
                        tagDownVote.setText(mTag.getmTagDownvoteCount()+1 + " Downvotes");
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Tag Voted Down Transaction failure.", e);
                                Toast.makeText(getApplicationContext(), "Tag Voted Down Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // code for showing tag image
        Picasso.with(this).load(mTag.getmTagImageURI()).into(tagImageView);

        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MAP_FRAG_IN_DETAIL);

        Location mTagLoc = new Location("");
        mTagLoc.setLatitude(mTag.getmTagLocationLat());
        mTagLoc.setLongitude(mTag.getmTagLocationLong());

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance(mTag, mTagLoc);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mapContainer, mapFragment, MAP_FRAG_IN_DETAIL)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .remove(mapFragment)
                    .add(R.id.mapContainer, MapFragment.newInstance(mTag, mTagLoc), MAP_FRAG_IN_DETAIL)
                    .commit();
            mapFragment.updateNewTagLocation(mTag, mTagLoc);
        }

        //TODO We might need to handle upvote/downvote inside this activity too
    }
}
