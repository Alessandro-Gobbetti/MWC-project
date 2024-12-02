package com.example.usimaps;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class ImageCaptureManager {
    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private final PreviewView previewView;
    private final DirectionManager directionManager;
    private final FusedLocationProviderClient fusedLocationClient;

    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;

    public interface ImageCaptureListener {
        void onImageCaptured(String imagePath);
        void onError(Exception exception);
    }

    private ImageCaptureListener imageCaptureListener;

    public ImageCaptureManager(Context context, LifecycleOwner lifecycleOwner, PreviewView previewView) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.previewView = previewView;
        this.directionManager = new DirectionManager(context);
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    // Initialize and start the camera
    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    // Method to capture image
    public void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "IMG_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        handleImageCaptured(photoFile.getAbsolutePath());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        if (imageCaptureListener != null) {
                            imageCaptureListener.onError(exception);
                        }
                    }
                });
    }

    // Handle the image capture and metadata storage
    private void handleImageCaptured(String imagePath) {
        getCurrentLocation(location -> {
            double latitude = location != null ? location.getLatitude() : 0.0;
            double longitude = location != null ? location.getLongitude() : 0.0;
            float direction = directionManager.getAzimuth();
            long timestamp = System.currentTimeMillis();

            saveImageMetadata(imagePath, latitude, longitude, direction, timestamp);

            if (imageCaptureListener != null) {
                imageCaptureListener.onImageCaptured(imagePath);
            }
        });
    }

    // Get current location
    private void getCurrentLocation(OnLocationRetrievedListener listener) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            listener.onLocationRetrieved(null);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> listener.onLocationRetrieved(location))
                .addOnFailureListener(e -> listener.onLocationRetrieved(null));
    }

    private interface OnLocationRetrievedListener {
        void onLocationRetrieved(Location location);
    }

    // Save image metadata to database
    private void saveImageMetadata(String imagePath, double latitude, double longitude, float direction, long timestamp) {
        ImageDatabaseHelper dbHelper = new ImageDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ImageDatabaseHelper.COLUMN_IMAGE_PATH, imagePath);
        values.put(ImageDatabaseHelper.COLUMN_LATITUDE, latitude);
        values.put(ImageDatabaseHelper.COLUMN_LONGITUDE, longitude);
        values.put(ImageDatabaseHelper.COLUMN_DIRECTION, direction);
        values.put(ImageDatabaseHelper.COLUMN_TIMESTAMP, timestamp);

        long newRowId = db.insert(ImageDatabaseHelper.TABLE_IMAGES, null, values);

        if (newRowId == -1) {
            // Handle failure if necessary
        }

        db.close();
    }

    // Set the image capture listener
    public void setImageCaptureListener(ImageCaptureListener listener) {
        this.imageCaptureListener = listener;
    }

    // Start and stop direction manager
    public void startListeningToDirection() {
        directionManager.startListening();
    }

    public void stopListeningToDirection() {
        directionManager.stopListening();
    }

    // Cleanup method
    public void shutdown() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}

