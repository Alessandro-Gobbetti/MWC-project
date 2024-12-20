package com.example.usimaps.ui.navigate;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.usimaps.R;
import com.example.usimaps.databinding.FragmentRouteListBinding;
import com.example.usimaps.map.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Route List Fragment
 */
public class RouteListFragment extends Fragment {
    // Path and instructions
    private List<Vertex> path;
    private List<String> instructions;
    // binding
    private FragmentRouteListBinding binding;
    private RecyclerViewAdapter adapter;

    /**
     * Default constructor
     */
    public RouteListFragment() {
        // Required empty public constructor
        this.path = new ArrayList<>();
        this.instructions = new ArrayList<>();
    }

    /**
     * Constructor
     * @param path Path
     * @param instructions Instructions
     */
    public RouteListFragment(List<Vertex> path, List<String> instructions) {
        this.path = path;
        this.instructions = instructions;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRouteListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        this.adapter = new RecyclerViewAdapter(path, instructions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        return root;
    }

    /**
     * Update the path
     * @param path Path
     * @param instructions Instructions
     */
    public void updatePath(List<Vertex> path, List<String> instructions) {
        this.path = path;
        this.instructions = instructions;

        adapter.updatePath(path, instructions);
    }

}