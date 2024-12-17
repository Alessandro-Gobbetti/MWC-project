package com.example.usimaps.ui.home;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usimaps.DatabaseHelper;
import com.example.usimaps.ImageCaptureManager;
import com.example.usimaps.LocationAdapter;
import com.example.usimaps.databinding.FragmentHomeBinding;
import com.example.usimaps.map.Graph;
import com.example.usimaps.map.Vertex;
import com.example.usimaps.map.VertexType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import com.example.usimaps.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    //Fields needed for camera
    private PreviewView previewView;
    private MaterialButton buttonReturnToForm;
    private Button imageCaptureButton;

    private ImageCaptureManager imageCaptureManager;

    // Fields needed for form
    private TextInputEditText editTextName;
    private MaterialAutoCompleteTextView autoCompleteFloor;
    private MaterialAutoCompleteTextView autoCompleteEdge;
    private MaterialAutoCompleteTextView autoCompleteType;

    private TextInputEditText textGPS;


    private MaterialButton buttonSelectImage;
    private MaterialButton buttonShowCamera;
    private MaterialButton buttonSubmitForm;

    private ImageView selectedImageView;

    private final List<String> selectedEdges = new ArrayList<>();
    private ChipGroup chipGroupSelectedEdges;

    private String capturedImagePath = "";

    private double latitude;
    private double longitude;


    //TODO: Read graph from db
    private Graph graph = new Graph();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        graph = graph.generateUSIMap();

        // Initialize form fields
        editTextName = binding.editTextRoomName;
        textGPS = binding.editTextGPS;
        textGPS.setVisibility(View.GONE);

        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(checkName(s.toString())){
//                    Toast.makeText(getContext(), "Text: " + s, Toast.LENGTH_SHORT).show();
//                }else {
//                    Toast.makeText(getContext(), "Text no name:  " + s, Toast.LENGTH_SHORT).show();
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(checkName(s.toString())){
                    editTextName.setError("Name must be unique");
                    editTextName.requestFocus();
                }
            }
        });

        autoCompleteFloor = binding.autoCompleteFloor;
        autoCompleteEdge = binding.autoCompleteEdge;
        chipGroupSelectedEdges = binding.chipGroupSelectedEdges;
        autoCompleteType = binding.autoCompleteType;

        // Setup dropdown adapters
        //TODO: Move adapters to separate method
        ArrayAdapter<String> floorAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                getFloors()
        );
        autoCompleteFloor.setAdapter(floorAdapter);

        ArrayAdapter<String> edgeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                getEdgeNames()
        );
        autoCompleteEdge.setAdapter(edgeAdapter);

        autoCompleteEdge.setOnItemClickListener((parent, view, position, id) -> {
            String selectedEdge = (String) parent.getItemAtPosition(position);

            // Check if already selected
            if (!selectedEdges.contains(selectedEdge)) {
                selectedEdges.add(selectedEdge); // Add to selected list
                addChip(selectedEdge);           // Add Chip
            }

            // Clear input field
            autoCompleteEdge.setText("");
        });

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                getTypes()
        );
        autoCompleteType.setAdapter(typeAdapter);

        buttonSelectImage = binding.buttonSelectImage;
        buttonSelectImage.setVisibility(View.GONE);
        selectedImageView = binding.imageViewSelected;
        selectedImageView.setVisibility(View.GONE);

        // Register the photo picker launcher
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        selectedImageView.setImageURI(uri); // Display the selected image
                    } else {
                        Toast.makeText(getContext(), "No media selected", Toast.LENGTH_SHORT).show();
                    }
                });

        buttonShowCamera = binding.buttonShowCamera;
        buttonSubmitForm = binding.buttonSubmitForm;

        //Camera
        previewView = binding.cameraPreview;
        buttonReturnToForm = binding.buttonReturnToForm;

        imageCaptureButton = binding.imageCaptureButton;

        // Initialize ImageCaptureManager
        imageCaptureManager = new ImageCaptureManager(requireContext(), getViewLifecycleOwner(), previewView);

        imageCaptureManager.setImageCaptureListener(new ImageCaptureManager.ImageCaptureListener() {
            @Override
            public void onImageCaptured(String imagePath, double latitude, double longitude,
                                        float direction, long timestamp) {

                String gpsText = String.format("Latitude: %.6f, Longitude: %.6f", latitude, longitude);
                capturedImagePath = imagePath;
                textGPS.setVisibility(View.VISIBLE);
                textGPS.setText(gpsText);
                binding.cameraContainer.setVisibility(View.GONE);
                binding.formContainer.setVisibility(View.VISIBLE);
                selectedImageView.setImageURI(Uri.parse(imagePath));
                selectedImageView.setVisibility(View.VISIBLE);

                // Save the GPS coordinates
                HomeFragment.this.latitude = latitude;
                HomeFragment.this.longitude = longitude;

            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(requireContext(), "Failed to save photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

//        imageCaptureManager.startCamera();

        // Set initial visibility: show form, hide camera
        binding.formContainer.setVisibility(View.VISIBLE);
        binding.cameraContainer.setVisibility(View.GONE);

        buttonSelectImage.setOnClickListener(v -> pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));

        // Handle showing the camera
        buttonShowCamera.setOnClickListener(v -> {
            // Hide form, show camera
            binding.formContainer.setVisibility(View.GONE);
            binding.cameraContainer.setVisibility(View.VISIBLE);

            // Start the camera
//            imageCaptureManager.startCamera();
        });

        // Handle returning to form
        buttonReturnToForm.setOnClickListener(v -> {
            // Hide camera, show form
            binding.cameraContainer.setVisibility(View.GONE);
            binding.formContainer.setVisibility(View.VISIBLE);

            // Stop the camera
//            imageCaptureManager.shutdown();
        });

        buttonSubmitForm.setOnClickListener(v -> submitForm());

        binding.imageCaptureButton.setOnClickListener(v -> {
            imageCaptureManager.takePhoto();
//            imageCaptureManager.shutdown();
        });

        return root;
    }

    private void addChip(String edge) {
        // Create a new Chip
        Chip chip = new Chip(requireContext());
        chip.setText(edge);
        chip.setCloseIconVisible(true); // Show the close icon
        chip.setOnCloseIconClickListener(v -> {
            // Remove the chip and update the list
            chipGroupSelectedEdges.removeView(chip);
            selectedEdges.remove(edge);
        });

        // Add the chip to the ChipGroup
        chipGroupSelectedEdges.addView(chip);
    }

    private void submitForm() {
        String name = editTextName.getText() != null ? editTextName.getText().toString().trim() : "";
        String floor = autoCompleteFloor.getText() != null ? autoCompleteFloor.getText().toString().trim() : "";
        String type = autoCompleteType.getText() != null ? autoCompleteType.getText().toString().trim(): "";

        // Simple validation
        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Room name required");
            editTextName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(floor) || !getFloors().contains(floor)) {
            // FIXME: Check if floor is valid before
            autoCompleteFloor.setError("Floor required");
            autoCompleteFloor.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(type)){
            autoCompleteType.setError("Type is required");
            autoCompleteType.requestFocus();
            return;
        }

        if (selectedEdges.isEmpty()) {
            autoCompleteEdge.setError("Connected edges required");
            autoCompleteEdge.requestFocus();
            return;
        }

        if (capturedImagePath.isEmpty()){
            imageCaptureButton.setError("Image is required !");
            imageCaptureButton.requestFocus();
            return;
        }

        // TODO: If image was taken or selected, use that too
        // TODO: Save form data to db and update map.
        // TODO: use type too

        int floorInt = graph.ordinalFloorToInt(floor);
        VertexType vertexType = VertexType.valueOf(type);
        // new Vertex("Corridor D0", VertexType.CONNECTION, 46.012324, 8.961444, 0);
        Vertex vertex = new Vertex(name, vertexType, this.latitude, this.longitude, floorInt, capturedImagePath);
        graph.addVertex(vertex);

        for (String edgeName : selectedEdges) {
            graph.connectVertexToEdgeByName(vertex, edgeName);
        }

        // Save the graph to the database
        DatabaseHelper db = new DatabaseHelper(requireContext());
        db.updateGraph(graph);

        Toast.makeText(requireContext(), "Form submitted: " + name, Toast.LENGTH_SHORT).show();
        // Clear the form
        clearForm();
    }

    //TODO: Update these getters with actual values from the map
    private List<String> getEdgeNames(){
        List<String> arr = new ArrayList<>(graph.getEdgeNames());
        return arr;
    }

    private List<String> getFloors(){
        return graph.getFloorNames();
    }

    private List<String> getTypes(){
        List<String> arr = new ArrayList<>();
        for(VertexType c : VertexType.values())
            arr.add(c.toString());
        return arr;
    }

    private boolean checkName(String name){
        List<String> allNames = graph.getSearchableNames(); // TODO: Handle edges
        return allNames.contains(name);
    }

    private void clearForm() {
        editTextName.setText("");
        autoCompleteFloor.setText("");
        autoCompleteEdge.setText("");
        autoCompleteType.setText("");
        selectedEdges.clear();
        chipGroupSelectedEdges.removeAllViews();
        selectedImageView.setImageURI(null);
        capturedImagePath = "";
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start listening for sensor updates
        imageCaptureManager.startListeningToDirection();
        imageCaptureManager.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop listening for sensor updates to save battery
        imageCaptureManager.stopListeningToDirection();
        // Re-initialize the camera
        imageCaptureManager.shutdown();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imageCaptureManager.shutdown();
        binding = null;
    }
}
