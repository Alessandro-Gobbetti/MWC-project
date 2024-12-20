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

/**
 * The ImageCaptureManager class manages camera functionality, including starting the camera,
 * capturing photos, and storing metadata such as location and direction. It uses CameraX for
 * camera operations and Google's Fused Location API for retrieving GPS coordinates.
 */
public class ImageCaptureManager {

    /**
     * The context in which this manager operates.
     */
    private final Context context;

    /**
     * The LifecycleOwner associated with the camera operations.
     */
    private final LifecycleOwner lifecycleOwner;

    /**
     * The PreviewView used for displaying the camera preview.
     */
    private final PreviewView previewView;

    /**
     * Manages direction sensing to retrieve azimuth values.
     */
    private final DirectionManager directionManager;

    /**
     * Client used for retrieving the user's current location.
     */
    private final FusedLocationProviderClient fusedLocationClient;

    /**
     * Handles capturing images.
     */
    private ImageCapture imageCapture;

    /**
     * The ProcessCameraProvider that binds camera use cases to the lifecycle.
     */
    private ProcessCameraProvider cameraProvider;

    /**
     * Listener interface for image capture events.
     */
    public interface ImageCaptureListener {
        /**
         * Called when an image is successfully captured.
         *
         * @param imagePath  The file path where the image is saved.
         * @param latitude   The latitude where the image was taken.
         * @param longitude  The longitude where the image was taken.
         * @param direction  The azimuth direction when the image was taken.
         * @param timestamp  The timestamp of the image capture.
         */
        void onImageCaptured(String imagePath, double latitude, double longitude,
                             float direction, long timestamp);

        /**
         * Called when an error occurs during image capture.
         *
         * @param exception The exception describing the error.
         */
        void onError(Exception exception);
    }

    private ImageCaptureListener imageCaptureListener;

    /**
     * Constructs a new ImageCaptureManager instance.
     *
     * @param context       The context in which the manager operates.
     * @param lifecycleOwner The lifecycle owner associated with camera operations.
     * @param previewView   The PreviewView used for displaying the camera preview.
     */
    public ImageCaptureManager(Context context, LifecycleOwner lifecycleOwner, PreviewView previewView) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.previewView = previewView;
        this.directionManager = new DirectionManager(context);
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Starts the camera and binds it to the lifecycle.
     */
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

    /**
     * Captures an image and saves it to the local file system.
     */
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
                imageCaptureListener.onImageCaptured(imagePath, latitude, longitude, direction, timestamp);
            }
        });
    }

    /**
     * Retrieves the current location using the Fused Location API.
     *
     * @param listener A listener that receives the retrieved location or null if unavailable.
     */
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

    /**
     * Interface for listening to location retrieval events.
     */
    private interface OnLocationRetrievedListener {
        void onLocationRetrieved(Location location);
    }

    /**
     * Saves image metadata to the local database.
     *
     * @param imagePath  The file path of the captured image.
     * @param latitude   The latitude where the image was taken.
     * @param longitude  The longitude where the image was taken.
     * @param direction  The azimuth direction when the image was taken.
     * @param timestamp  The timestamp of the image capture.
     */
    private void saveImageMetadata(String imagePath, double latitude, double longitude, float direction, long timestamp) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IMAGE_PATH, imagePath);
        values.put(DatabaseHelper.COLUMN_LATITUDE, latitude);
        values.put(DatabaseHelper.COLUMN_LONGITUDE, longitude);
        values.put(DatabaseHelper.COLUMN_DIRECTION, direction);
        values.put(DatabaseHelper.COLUMN_TIMESTAMP, timestamp);

        long newRowId = db.insert(DatabaseHelper.TABLE_IMAGES, null, values);

        if (newRowId == -1) {
            // Handle failure if necessary
        }

        db.close();
    }

    /**
     * Sets a listener for image capture events.
     *
     * @param listener The listener to be notified of capture events.
     */
    public void setImageCaptureListener(ImageCaptureListener listener) {
        this.imageCaptureListener = listener;
    }

    /**
     * Starts the direction manager to listen for direction changes.
     */
    public void startListeningToDirection() {
        directionManager.startListening();
    }

    /**
     * Stops the direction manager from listening for direction changes.
     */
    public void stopListeningToDirection() {
        directionManager.stopListening();
    }

    /**
     * Cleans up resources, such as unbinding the camera provider.
     */
    public void shutdown() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}

