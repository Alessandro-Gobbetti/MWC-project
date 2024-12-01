package com.example.usimaps.ui.home;

import android.Manifest;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import android.content.pm.PackageManager;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usimaps.DirectionManager;
import com.example.usimaps.ImageDatabaseHelper;
import com.example.usimaps.MainActivity;
import com.example.usimaps.R;
import com.example.usimaps.databinding.FragmentHomeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import com.example.usimaps.LocationAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private DirectionManager directionManager;
    private SearchBar fromSearchBar, toSearchBar;
    private SearchView fromSearchView, toSearchView;

    private List<String> locationSuggestions;
    private String selectedFromLocation, selectedToLocation;

    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    private BarcodeScanner barcodeScanner;

    private static final int REQUEST_CODE_PERMISSIONS = 45; // Figure out exactly what this does


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        previewView = binding.cameraPreview;

        fromSearchBar = binding.fromSearchBar;
        fromSearchView = binding.fromSearchView;

        toSearchBar = binding.toSearchBar;
        toSearchView = binding.toSearchView;

        locationSuggestions = getLocationSuggestions();

        directionManager = new DirectionManager(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();

        barcodeScanner = BarcodeScanning.getClient(options);

        startCamera();

        binding.imageCaptureButton.setOnClickListener(v ->takePhoto());

        // Set up the from search bar and view
        setupSearchBarAndView(fromSearchBar, fromSearchView, true);

//        fromSearchBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                if (item.getItemId() == binding.) {
//                    // Clear the text in the search bar and search view
//                    fromSearchBar.setText("");
//                    fromSearchView.getEditText().setText("");
//                    return true;
//                }
//                return false;
//            }
//        });

        fromSearchBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_qr_scan) {
                Toast.makeText(requireContext(), "Open QR", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        // Set up the to search bar and view
        setupSearchBarAndView(toSearchBar, toSearchView, false);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start listening for sensor updates
        directionManager.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop listening for sensor updates to save battery
        directionManager.stopListening();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        imageCapture = new ImageCapture.Builder().build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().
                setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), image -> processImageForQRCode(image));

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture,imageAnalysis);
    }

    private void processImageForQRCode(ImageProxy image){
        try {
            try {
                @SuppressWarnings("UnsafeOptInUsageError")
                InputImage inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());

                barcodeScanner.process(inputImage)
                        .addOnSuccessListener(barcodes -> {
                            for (Barcode barcode : barcodes) {
                                String rawValue = barcode.getRawValue();
                                if (rawValue != null) {
                                    Toast.makeText(requireContext(), "QR Code Data: " + rawValue, Toast.LENGTH_SHORT).show();
                                    // Handle the QR code data as needed
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Failed to process QR code", Toast.LENGTH_SHORT).show();
                        })
                        .addOnCompleteListener(task -> image.close()); // Ensure the image is closed after processing
            } catch (Exception e) {
                image.close();
                e.printStackTrace();
            }

        } catch (Exception e){
            image.close();
            e.printStackTrace();
        }
    }

    private void getCurrentLocation() {
        //Handle this permission check better
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        System.out.println("Fetching location");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        System.out.println("Location fetched");
                        currentLocation = location; // Save the location
                    } else {
                        Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to fetch location", Toast.LENGTH_SHORT).show());
    }

    private void saveImageMetadata(String imagePath, double latitude, double longitude, float direction, long timestamp) {
        ImageDatabaseHelper dbHelper = new ImageDatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ImageDatabaseHelper.COLUMN_IMAGE_PATH, imagePath);
        values.put(ImageDatabaseHelper.COLUMN_LATITUDE, latitude);
        values.put(ImageDatabaseHelper.COLUMN_LONGITUDE, longitude);
        values.put(ImageDatabaseHelper.COLUMN_DIRECTION, direction);
        values.put(ImageDatabaseHelper.COLUMN_TIMESTAMP, timestamp);

        long newRowId = db.insert(ImageDatabaseHelper.TABLE_IMAGES, null, values);
        System.out.println(values);

        if (newRowId == -1) {
            Toast.makeText(requireContext(), "Failed to save metadata", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Metadata saved successfully", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "IMG_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        String imagePath = photoFile.getAbsolutePath();
                        getCurrentLocation();

                        double latitude = currentLocation != null ? currentLocation.getLatitude() : 0.0;
                        double longitude = currentLocation != null ? currentLocation.getLongitude() : 0.0;

                        float direction = directionManager.getAzimuth(); // understand how to work with the float
                        long timestamp = System.currentTimeMillis();

                        saveImageMetadata(imagePath, latitude, longitude, direction, timestamp);

                        Toast.makeText(requireContext(), "Photo saved at: " + imagePath, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(requireContext(), "Failed to save photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (barcodeScanner != null){
            barcodeScanner.close();
        }
        binding = null;
    }

    // TODO: Use this for direction.
    private String convertAzimuthToDirection(float azimuth) {
        if (azimuth >= 337.5 || azimuth < 22.5) return "North";
        else if (azimuth >= 22.5 && azimuth < 67.5) return "Northeast";
        else if (azimuth >= 67.5 && azimuth < 112.5) return "East";
        else if (azimuth >= 112.5 && azimuth < 157.5) return "Southeast";
        else if (azimuth >= 157.5 && azimuth < 202.5) return "South";
        else if (azimuth >= 202.5 && azimuth < 247.5) return "Southwest";
        else if (azimuth >= 247.5 && azimuth < 292.5) return "West";
        else return "Northwest";
    }

    private void setupSearchBarAndView(SearchBar searchBar, SearchView searchView, boolean isFrom) {
//        // Connect the search bar and search view
//        searchBar.setupWithSearchView(searchView);

        // Get the RecyclerView from the SearchView
        RecyclerView recyclerView = isFrom ? binding.fromSearchRecyclerView : binding.toSearchRecyclerView;

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create an adapter
        LocationAdapter adapter = new LocationAdapter(getContext(), new ArrayList<>(locationSuggestions));

        // Set the adapter to the RecyclerView
        recyclerView.setAdapter(adapter);

        // Set up the query text listener to filter suggestions
        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) {
                // Filter suggestions based on input
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

        // Handle item click in suggestions
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
        // TODO: Fetch actual locations from map
        return Arrays.asList("New York", "Los Angeles", "Chicago", "Houston",
                "Phoenix", "Philadelphia", "San Antonio", "San Diego",
                "Dallas", "San Jose");
    }

    private void fromLocationSelected(String location) {
        // Called when the "from" location is selected
        Toast.makeText(requireContext(), "From location selected", Toast.LENGTH_SHORT).show();

        // Check if both locations are selected
        checkLocationsSelected();
    }

    private void toLocationSelected(String location) {
        // Called when the "to" location is selected
        Toast.makeText(requireContext(), "To location selected", Toast.LENGTH_SHORT).show();

        // Check if both locations are selected
        checkLocationsSelected();
    }

    private void checkLocationsSelected() {
        String fromLocation = fromSearchBar.getText().toString();
        String toLocation = toSearchBar.getText().toString();
        if (!fromLocation.isEmpty() && !toLocation.isEmpty()) {
            // Both locations are selected


            locationsSelected(fromLocation, toLocation);
        }
    }

    private void locationsSelected(String fromLocation, String toLocation) {
        // Handle the case when both locations are selected
        Toast.makeText(requireContext(), "Both locations selected", Toast.LENGTH_SHORT).show();
    }
}