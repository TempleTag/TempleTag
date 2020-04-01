package edu.temple.templetag.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.temple.templetag.R;
import edu.temple.templetag.Tag;
import io.opencensus.tags.Tags;

public class TagRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ArrayList<Tag> Tags;

    public TagRecyclerViewAdapter(Context context, ArrayList<Tag> Tags){
        this.context = context;
        this.Tags = (ArrayList<Tag>) Tags.clone();
    }

    public void updateDataSet(ArrayList<Tag> Tags){
        this.Tags = (ArrayList<Tag>) Tags.clone();
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((TagView)holder).tagName.setText(Tags.get(position).getmTagLocationName());
        ((TagView)holder).tagDesc.setText(Tags.get(position).getmTagDescription());
        ((TagView)holder).tagPop.setText(Tags.get(position).getmTagPopularity() + " people are talking about this.");
        ((TagView)holder).tagUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "You clicked Up Vote", Toast.LENGTH_SHORT).show();
            }
        });

        ((TagView)holder).tagDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "You clicked Down Vote", Toast.LENGTH_SHORT).show();
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

        public TagView(@NonNull View itemView) {
            super(itemView);
            tagImgView = itemView.findViewById(R.id.tag_icon);
            tagName = itemView.findViewById(R.id.tag_location_name);
            tagDesc = itemView.findViewById(R.id.tag_description);
            tagPop = itemView.findViewById(R.id.tag_popularity);
            tagUp = itemView.findViewById(R.id.tag_up_vote);
            tagDown = itemView.findViewById(R.id.tag_down_vote);
        }
    }
}
