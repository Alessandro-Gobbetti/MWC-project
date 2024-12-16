package com.example.usimaps.ui.slideshow;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usimaps.HistoryAdapter;
import com.example.usimaps.R;
import com.example.usimaps.RecyclerViewAdapter;
import com.example.usimaps.databinding.FragmentHistoryListBinding;
import com.example.usimaps.databinding.FragmentRouteListBinding;
import com.example.usimaps.map.Vertex;
import com.example.usimaps.ui.gallery.GalleryFragment;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;
import kotlin.Triple;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class RouteHistoryListFragment extends Fragment {

    private List<Triple<String, String, String>> history = new ArrayList<>();

    private FragmentHistoryListBinding binding;
    private HistoryAdapter adapter;
    
    public RouteHistoryListFragment() {
        // Required empty public constructor
    }


    public RouteHistoryListFragment(List<Triple<String, String, String>> history) {
        this.history = history;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.historyList;
        this.adapter = new HistoryAdapter(history, item -> {
            System.out.println("Clicked History Item List");
            // handle click item
            Bundle bundle = new Bundle();
            bundle.putString("start", item.getSecond());
            bundle.putString("goal", item.getThird());
            Navigation.findNavController(root).navigate(R.id.nav_gallery, bundle);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        return root;
    }

}