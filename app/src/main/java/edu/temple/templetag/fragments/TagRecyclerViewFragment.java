package edu.temple.templetag.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import edu.temple.templetag.R;
import edu.temple.templetag.Tag;
import edu.temple.templetag.adapters.TagRecyclerViewAdapter;

public class TagRecyclerViewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG_LIST = "tagList";
    RecyclerView tagRecyclerView;
    TagRecyclerViewAdapter tagRecyclerViewAdapter;

    // TODO: Rename and change types of parameters
    private ArrayList<Tag> Tags;

    public TagRecyclerViewFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TagRecyclerViewFragment newInstance(ArrayList<Tag> Tags) {
        TagRecyclerViewFragment fragment = new TagRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(TAG_LIST, Tags);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Tags = getArguments().getParcelableArrayList(TAG_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tag_recycler_view, container, false);
        tagRecyclerView = view.findViewById(R.id.recycler_view);
        tagRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tagRecyclerViewAdapter = new TagRecyclerViewAdapter(getContext(), Tags);

        tagRecyclerView.setAdapter(tagRecyclerViewAdapter);
        return view;
    }

    public void updateDataSet(ArrayList<Tag> Tags){
        this.Tags = (ArrayList<Tag>) Tags.clone();
        tagRecyclerViewAdapter.updateDataSet(this.Tags);
    }
}
