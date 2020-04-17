package edu.temple.templetag.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import edu.temple.templetag.R;
import edu.temple.templetag.Tag;
import edu.temple.templetag.TagDetailActivity;

public class TagRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ArrayList<Tag> Tags;
    public FirebaseFirestore firestore;

    public static final String SELECTED_TAG = "theTag";

    public TagRecyclerViewAdapter(Context context, ArrayList<Tag> Tags) {
        this.context = context;
        this.Tags = Tags;
    }

    public void updateDataSet(ArrayList<Tag> Tags) {
        this.Tags = Tags;
        notifyDataSetChanged();
    }

    public void clearDataSet() {
        this.Tags = null;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View tag_item = inflater.inflate(R.layout.tag_item, parent, false);
        TagView tag_view = new TagView(tag_item);
        return tag_view;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        firestore = FirebaseFirestore.getInstance();
        final DocumentReference tagRef = firestore
                .collection("Tags")
                .document(Tags.get(position).getmTagID());
        ((TagView)holder).tagName.setText(Tags.get(position).getmTagLocationName());
        ((TagView)holder).tagDesc.setText(Tags.get(position).getmTagDescription());
        if (Tags.get(position).getmTagPopularity() == 1) {
            ((TagView)holder).tagPop.setText(Tags.get(position).getmTagPopularity() + " person is talking about this event");
        } else {
            ((TagView)holder).tagPop.setText(Tags.get(position).getmTagPopularity() + " people are talking about this event");
        }
        Picasso.with(context).load(Tags.get(position).getmTagImageURI()).into(((TagView)holder).tagImgView);
        ((TagView)holder).tagUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tagRef.update(
                        "upvoteCount", Tags.get(position).getmTagUpvoteCount()+1,
                        "popularityCount", Tags.get(position).getmTagPopularity()+1
                ).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Tag Voted Up", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context, "Up Vote Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        ((TagView)holder).tagDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagRef.update(
                        "downvoteCount", Tags.get(position).getmTagDownvoteCount()+1,
                        "popularityCount", Tags.get(position).getmTagPopularity()-1
                ).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Tag Voted Down", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context, "Down Vote failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        ((TagView)holder).tagItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //When tag is selected
                Intent intent = new Intent(context, TagDetailActivity.class);
                intent.putExtra(SELECTED_TAG, Tags.get(position));
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return Tags.size();
    }

    public class TagView extends RecyclerView.ViewHolder{
        de.hdodenhof.circleimageview.CircleImageView tagImgView;
        TextView tagName, tagDesc, tagPop;
        ImageButton tagUp, tagDown;
        RelativeLayout tagItemLayout;

        public TagView(@NonNull View itemView) {
            super(itemView);
            tagImgView = itemView.findViewById(R.id.tag_icon);
            tagName = itemView.findViewById(R.id.tag_location_name);
            tagDesc = itemView.findViewById(R.id.tag_description);
            tagPop = itemView.findViewById(R.id.tag_popularity);
            tagUp = itemView.findViewById(R.id.tag_up_vote);
            tagDown = itemView.findViewById(R.id.tag_down_vote);
            tagItemLayout = itemView.findViewById(R.id.tagItemLayout);
        }
    }
}
