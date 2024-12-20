package com.example.usimaps.ui.edit;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.usimaps.DatabaseHelper;
import com.example.usimaps.ImageCaptureManager;
import com.example.usimaps.databinding.FragmentEditBinding;
import com.example.usimaps.map.Graph;
import com.example.usimaps.map.Vertex;
import com.example.usimaps.map.VertexType;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment that allows users to edit and add new locations (vertices) to the map.
 *
 * The fragment provides a form where users can input a room name, select its floor,
 * connected edges, and type. Users have to capture an image for the location, which also
 * records their current GPS coordinates.
 * The data is validated and saved to a database upon submission.
 */
public class EditFragment extends Fragment {

    private FragmentEditBinding binding;

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


    private MaterialButton buttonShowCamera;
    private MaterialButton buttonSubmitForm;

    private ImageView selectedImageView;

    private final List<String> selectedEdges = new ArrayList<>();
    private ChipGroup chipGroupSelectedEdges;

    private String capturedImagePath = "";

    private double latitude;
    private double longitude;

    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;

    //TODO: Read graph from db
    private Graph graph = new Graph();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentEditBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        DatabaseHelper db = new DatabaseHelper(getContext());
        Graph loadedGraph = db.loadGraph("USI Campus EST");
        if (loadedGraph != null) {
            this.graph = loadedGraph;
            System.out.println("Graph loaded: " + graph.getMapName());
        } else {
            this.graph = graph.generateUSIMap();
        }

        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        Log.e("Edit Fragment: ", "Camera permission is required.");
                    }
                });

        requestLocationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        Log.e("Edit Fragment: ", "Location permission is required.");
                    }
                });

        // Initialize form fields
        editTextName = binding.editTextRoomName;
        textGPS = binding.editTextGPS;
        textGPS.setVisibility(View.GONE);

        autoCompleteFloor = binding.autoCompleteFloor;
        autoCompleteEdge = binding.autoCompleteEdge;
        chipGroupSelectedEdges = binding.chipGroupSelectedEdges;
        autoCompleteType = binding.autoCompleteType;

        selectedImageView = binding.imageViewSelected;
        selectedImageView.setVisibility(View.GONE);

        buttonShowCamera = binding.buttonShowCamera;
        buttonSubmitForm = binding.buttonSubmitForm;

        //Camera
        previewView = binding.cameraPreview;
        buttonReturnToForm = binding.buttonReturnToForm;

        imageCaptureButton = binding.imageCaptureButton;

        // Initialize ImageCaptureManager
        imageCaptureManager = new ImageCaptureManager(requireContext(), getViewLifecycleOwner(), previewView);

        // Set initial visibility: show form, hide camera
        binding.formContainer.setVisibility(View.VISIBLE);
        binding.cameraContainer.setVisibility(View.GONE);

        setupDropdownAdapters();
        setupListeners();

        return root;
    }

    /**
     * Sets up listeners for various UI elements, including input fields and buttons.
     *
     * Handles image capture, form submission, validation, and permissions requests.
     */
    private void setupListeners(){
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
                buttonShowCamera.setText("Retake Photo");

                // Save the GPS coordinates
                EditFragment.this.latitude = latitude;
                EditFragment.this.longitude = longitude;

                // Stop the camera
                imageCaptureManager.shutdown();
            }

            @Override
            public void onError(Exception exception) {
                Log.e("Edit Fragment: ", "Failed to save photo: " + exception.getMessage());
                imageCaptureManager.shutdown();
            }
        });

        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(checkName(s.toString())){
                    editTextName.setError("Name must be unique");
                    editTextName.requestFocus();
                }
            }
        });

        autoCompleteType.setOnClickListener(v -> {
            if(autoCompleteType.getError()!=null){
                autoCompleteType.setError(null);
                binding.textInputType.setErrorEnabled(false);
                binding.textInputType.setError(null);
            }
        });

        autoCompleteFloor.setOnClickListener(v -> {
            if(autoCompleteFloor.getError()!=null){
                autoCompleteFloor.setError(null);
                binding.textInputFloor.setError(null);
                binding.textInputFloor.setErrorEnabled(false);
            }
        });

        autoCompleteEdge.setOnItemClickListener((parent, view, position, id) -> {
            String selectedEdge = (String) parent.getItemAtPosition(position);

            if (autoCompleteEdge.getError()!= null){
                autoCompleteEdge.setError(null);
                binding.textInputEdge.setError(null);
                binding.textInputEdge.setErrorEnabled(false);
            }

            // Check if already selected
            if (!selectedEdges.contains(selectedEdge)) {
                selectedEdges.add(selectedEdge); // Add to selected list
                addChip(selectedEdge);           // Add Chip
            }

            // Clear input field
            autoCompleteEdge.setText("");
        });

        buttonShowCamera.setOnClickListener(v -> {
            if (buttonShowCamera.getError()!= null){
                buttonShowCamera.setError(null);
            }
            checkAndRequestPermissions(this::openCamera);
        });

        // Handle returning to form
        buttonReturnToForm.setOnClickListener(v -> {
            // Hide camera, show form
            binding.cameraContainer.setVisibility(View.GONE);
            binding.formContainer.setVisibility(View.VISIBLE);

            // Stop the camera
            imageCaptureManager.shutdown();
        });

        buttonSubmitForm.setOnClickListener(v -> submitForm());

        binding.imageCaptureButton.setOnClickListener(v -> {
            imageCaptureManager.takePhoto();
        });

    }

    /**
     * Configures dropdown adapters for the form fields (e.g., floors, edges, and types).
     *
     * Populates the MaterialAutoCompleteTextView dropdowns with relevant data from the graph.
     */
    private void setupDropdownAdapters() {
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

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                getTypes()
        );
        autoCompleteType.setAdapter(typeAdapter);
    }

    /**
     * Opens the camera interface, hiding the form and showing the camera preview.
     *
     * This method is triggered when the user opts to capture an image.
     */
    private void openCamera(){
        // Hide form, show camera
        binding.formContainer.setVisibility(View.GONE);
        binding.cameraContainer.setVisibility(View.VISIBLE);

        // Start the camera
        imageCaptureManager.startCamera();
    }

    /**
     * Validates the user input in the form and submits the data to the database.
     *
     * Checks for required fields (room name, floor, connected edges, type, and image),
     * and if valid, adds the new vertex to the graph and database.
     */
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
            buttonShowCamera.setError("Image is required !");
            buttonShowCamera.requestFocus();
            return;
        }

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

        Log.i("Edit Fragment: ", "Form Submitted");
        // Clear the form
        clearForm();
    }

    /**
     * Adds a new chip to the ChipGroup for a selected edge.
     *
     * @param edge The name of the edge selected by the user.
     */
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

    /**
     * Retrieves a list of edge names from the graph for populating the dropdown.
     *
     * @return A list of edge names.
     */
    private List<String> getEdgeNames(){
        List<String> arr = new ArrayList<>(graph.getEdgeNames());
        return arr;
    }

    /**
     * Retrieves a list of floor names from the graph for populating the dropdown.
     *
     * @return A list of floor names.
     */
    private List<String> getFloors(){
        return graph.getFloorNames();
    }

    /**
     * Retrieves a list of vertex types for populating the dropdown.
     *
     * @return A list of vertex types as strings.
     */
    private List<String> getTypes(){
        List<String> arr = new ArrayList<>();
        for(VertexType c : VertexType.values())
            arr.add(c.toString());
        return arr;
    }

    /**
     * Retrieves a list of vertex types for populating the dropdown.
     *
     * @return A list of vertex types as strings.
     */
    private boolean checkName(String name){
        List<String> allNames = graph.getSearchableNames();
        return allNames.contains(name);
    }

    /**
     * Clears all input fields in the form, resetting the state.
     */
    private void clearForm() {
        editTextName.setText("");
        autoCompleteFloor.setText("");
        autoCompleteEdge.setText("");
        autoCompleteType.setText("");
        selectedEdges.clear();
        chipGroupSelectedEdges.removeAllViews();
        selectedImageView.setImageURI(null);
        selectedImageView.setVisibility(View.GONE);
        capturedImagePath = "";
        textGPS.setText("");
        textGPS.setVisibility(View.GONE);
        buttonShowCamera.setText("Take Photo");
    }

    /**
     * Checks for Camera and Location permissions, and if not granted, requests them.
     *
     * @param onPermissionsGranted A Runnable that executes when both permissions are granted.
     */
    private void checkAndRequestPermissions(Runnable onPermissionsGranted) {
        boolean isCameraGranted = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        boolean isLocationGranted = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (isCameraGranted && isLocationGranted) {
            // Both permissions are granted
            onPermissionsGranted.run();
        } else {
            boolean shouldShowCameraRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(), android.Manifest.permission.CAMERA);
            boolean shouldShowLocationRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);

            if (!isCameraGranted && !isLocationGranted) {
                // Both permissions missing
                if (shouldShowCameraRationale || shouldShowLocationRationale) {
                    showPermissionRationaleDialog(() -> {
                        requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
                        requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
                    });
                } else {
                    redirectToSettingsDialog();
                }
            } else if (!isCameraGranted) {
                // Only camera permission is missing
                if (shouldShowCameraRationale) {
                    showPermissionRationaleDialog(() -> requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA));
                } else {
                    redirectToSettingsDialog();
                }
            } else if (!isLocationGranted) {
                // Only location permission is missing
                if (shouldShowLocationRationale) {
                    showPermissionRationaleDialog(() -> requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION));
                } else {
                    redirectToSettingsDialog();
                }
            }
        }
    }

    /**
     * Displays a dialog explaining why permissions are required and requests them.
     *
     * @param onPositiveAction A Runnable to execute when the user agrees to grant permissions.
     */
    private void showPermissionRationaleDialog(Runnable onPositiveAction) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Permissions Required")
                .setMessage("This app requires Camera and Location permissions to function properly.")
                .setPositiveButton("Grant Permissions", (dialog, which) -> onPositiveAction.run())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Displays a dialog prompting the user to enable permissions from the app settings.
     */
    private void redirectToSettingsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Permissions Denied")
                .setMessage("Camera and/or Location permissions are required. Please enable them in the app settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
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
        // Re-initialize the camera
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imageCaptureManager.shutdown();
        binding = null;
    }
}
