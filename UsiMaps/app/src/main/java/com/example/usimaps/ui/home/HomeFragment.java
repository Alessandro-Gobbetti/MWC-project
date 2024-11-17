package com.example.usimaps.ui.home;

import android.Manifest;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.usimaps.DirectionManager;
import com.example.usimaps.ImageDatabaseHelper;
import com.example.usimaps.MainActivity;
import com.example.usimaps.databinding.FragmentHomeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private DirectionManager directionManager;

    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    private static final int REQUEST_CODE_PERMISSIONS = 45; // Figure out exactly what this does


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        previewView = binding.cameraPreview;

        startCamera();

        directionManager = new DirectionManager(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        binding.imageCaptureButton.setOnClickListener(v ->takePhoto());

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
        imageCapture = new ImageCapture.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);
    }

    private void getCurrentLocation() {
        //Handle this permission check better
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
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
        binding = null;
    }
}