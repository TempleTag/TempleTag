package edu.temple.templetag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

import edu.temple.templetag.adapters.TagRecyclerViewAdapter;
import edu.temple.templetag.fragments.MapFragment;

public class TagDetailActivity extends AppCompatActivity {

    TextView tagLocationName, tagCreatedBy, tagUpVote, tagDownVote, tagPop, tagDesc;
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
        tagPop.setText(mTag.getmTagPopularity() + " people are talking about this event");
        tagDesc.setText(mTag.getmTagDescription());

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
