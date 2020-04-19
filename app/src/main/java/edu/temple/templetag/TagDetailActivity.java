package edu.temple.templetag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.location.Location;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
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
    ImageButton delBtn;
    ImageView tagImageView;
    MapFragment mapFragment;


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
                Toast.makeText(TagDetailActivity.this, "Implement delete function with Firebase", Toast.LENGTH_SHORT).show();
                /*** TODO Add codes for delete button
                 *
                 *
                 *
                 * */
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

                tagRef.update(
                        "upvoteCount", mTag.getmTagUpvoteCount()+1
                ).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Tag Voted Up", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Up Vote Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        downvoteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tagRef.update(
                        "downvoteCount", mTag.getmTagDownvoteCount()+1
                ).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Tag Voted Down", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Down Vote Failed", Toast.LENGTH_SHORT).show();
                        }
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
                    .commitAllowingStateLoss();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mapContainer, mapFragment, MAP_FRAG_IN_DETAIL)
                    .commitAllowingStateLoss();
            mapFragment.updateNewTagLocation(mTag, mTagLoc);
        }

        //TODO We might need to handle upvote/downvote inside this activity too
    }
}
