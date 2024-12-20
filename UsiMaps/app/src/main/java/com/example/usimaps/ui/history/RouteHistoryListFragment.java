package com.example.usimaps.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usimaps.R;
import com.example.usimaps.databinding.FragmentHistoryListBinding;

import java.util.ArrayList;
import java.util.List;

import kotlin.Triple;

/**
 * The fragment for the history list
 */
public class RouteHistoryListFragment extends Fragment {

    // List of history items
    private List<Triple<String, String, String>> history = new ArrayList<>();
    // View binding
    private FragmentHistoryListBinding binding;
    // Adapter for the history list
    private HistoryAdapter adapter;

    /**
     * Constructor
     */
    public RouteHistoryListFragment() {
        // Required empty public constructor
    }

    /**
     * Constructor
     * @param history List of history items
     */
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
            Navigation.findNavController(root).navigate(R.id.action_history_to_navigate, bundle);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        return root;
    }

}