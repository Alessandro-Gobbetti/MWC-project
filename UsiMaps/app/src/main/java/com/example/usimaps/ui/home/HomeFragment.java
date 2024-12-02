package com.example.usimaps.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usimaps.ImageCaptureManager;
import com.example.usimaps.LocationAdapter;
import com.example.usimaps.databinding.FragmentHomeBinding;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private PreviewView previewView;
    private SearchBar fromSearchBar, toSearchBar;
    private SearchView fromSearchView, toSearchView;

    private List<String> locationSuggestions;
    private String selectedFromLocation, selectedToLocation;

    private ImageCaptureManager imageCaptureManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        previewView = binding.cameraPreview;

        // Initialize ImageCaptureManager
        imageCaptureManager = new ImageCaptureManager(requireContext(), getViewLifecycleOwner(), binding.cameraPreview);

        imageCaptureManager.setImageCaptureListener(new ImageCaptureManager.ImageCaptureListener() {
            @Override
            public void onImageCaptured(String imagePath) {
                Toast.makeText(requireContext(), "Photo saved at: " + imagePath, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(requireContext(), "Failed to save photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        imageCaptureManager.startCamera();

        binding.imageCaptureButton.setOnClickListener(v -> imageCaptureManager.takePhoto());

        fromSearchBar = binding.fromSearchBar;
        fromSearchView = binding.fromSearchView;

        toSearchBar = binding.toSearchBar;
        toSearchView = binding.toSearchView;

        locationSuggestions = getLocationSuggestions();

        // Set up the from search bar and view
        setupSearchBarAndView(fromSearchBar, fromSearchView, true);

        // Set up the to search bar and view
        setupSearchBarAndView(toSearchBar, toSearchView, false);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start listening for sensor updates
        imageCaptureManager.startListeningToDirection();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop listening for sensor updates to save battery
        imageCaptureManager.stopListeningToDirection();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imageCaptureManager.shutdown();
        binding = null;
    }

    private void setupSearchBarAndView(SearchBar searchBar, SearchView searchView, boolean isFrom) {
        RecyclerView recyclerView = isFrom ? binding.fromSearchRecyclerView : binding.toSearchRecyclerView;

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        LocationAdapter adapter = new LocationAdapter(getContext(), new ArrayList<>(locationSuggestions));

        recyclerView.setAdapter(adapter);

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) {
                String query = s.toString().toLowerCase();
                List<String> filteredList = new ArrayList<>();
                for (String location : locationSuggestions) {
                    if (location.toLowerCase().contains(query)) {
                        filteredList.add(location);
                    }
                }
                adapter.updateData(filteredList);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        adapter.setOnItemClickListener(selectedLocation -> {
            searchBar.setText(selectedLocation);
            searchView.getEditText().setText(selectedLocation);
            searchView.hide();
            if (isFrom) {
                fromLocationSelected(selectedLocation);
            } else {
                toLocationSelected(selectedLocation);
            }
        });
    }

    private List<String> getLocationSuggestions() {
        // TODO: Fetch actual locations from your map or database
        return Arrays.asList("New York", "Los Angeles", "Chicago", "Houston",
                "Phoenix", "Philadelphia", "San Antonio", "San Diego",
                "Dallas", "San Jose");
    }

    private void fromLocationSelected(String location) {
        Toast.makeText(requireContext(), "From location selected", Toast.LENGTH_SHORT).show();
        selectedFromLocation = location;
        checkLocationsSelected();
    }

    private void toLocationSelected(String location) {
        Toast.makeText(requireContext(), "To location selected", Toast.LENGTH_SHORT).show();
        selectedToLocation = location;
        checkLocationsSelected();
    }

    private void checkLocationsSelected() {
        String fromLocation = fromSearchBar.getText().toString();
        String toLocation = toSearchBar.getText().toString();
        if (!fromLocation.isEmpty() && !toLocation.isEmpty()) {
            locationsSelected(fromLocation, toLocation);
        }
    }

    private void locationsSelected(String fromLocation, String toLocation) {
        // Handle the case when both locations are selected
        Toast.makeText(requireContext(), "Both locations selected", Toast.LENGTH_SHORT).show();
    }
}
