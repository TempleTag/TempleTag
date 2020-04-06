package edu.temple.templetag;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import edu.temple.templetag.adapters.TagRecyclerViewAdapter;
import edu.temple.templetag.fragments.MapFragment;

public class TagDetailActivity extends AppCompatActivity {

    TextView tagLocationName, tagCreatedBy, tagUpVote, tagDownVote, tagPop, tagDesc;
    ImageButton closeBtn;
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

        tagLocationName.setText(mTag.getmTagLocationName());
        tagCreatedBy.setText("created by " + mTag.getmTagCreatedBy());
        tagUpVote.setText(mTag.getmTagUpvoteCount() + " Upvotes");
        tagDownVote.setText(mTag.getmTagDownvoteCount() + " Downvotes");
        tagPop.setText(mTag.getmTagPopularity() + " people are talking about this event");
        tagDesc.setText(mTag.getmTagDescription());

        closeBtn = findViewById(R.id.btn_delete);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TagDetailActivity.this, "Implement delete function with Firebase", Toast.LENGTH_SHORT).show();
            }
        });

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
            mapFragment.updateNewATag(mTag, mTagLoc);
        }
    }
}
