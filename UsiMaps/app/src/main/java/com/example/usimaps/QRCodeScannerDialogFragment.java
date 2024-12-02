package com.example.usimaps;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class QRCodeScannerDialogFragment extends DialogFragment {

    private PreviewView previewView;
    private BarcodeScanner barcodeScanner;
    private ProcessCameraProvider cameraProvider;
    private Set<String> validLocations;
    private String lastInvalidScannedValue = null;

    // Static
    public static QRCodeScannerDialogFragment newInstance(ArrayList<String> validLocations) {
        QRCodeScannerDialogFragment fragment = new QRCodeScannerDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("valid_locations", validLocations);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve validLocations from arguments
        if (getArguments() != null) {
            ArrayList<String> validLocationsList = getArguments().getStringArrayList("valid_locations");
            validLocations = new HashSet<>();
            assert validLocationsList != null;
            for (String location : validLocationsList) {
                validLocations.add(location.toLowerCase().trim());
            }
        } else {
            validLocations = new HashSet<>();
            Toast.makeText(requireContext(), "No valid locations passed: ", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_qrcode_scanner, container, false);

        previewView = view.findViewById(R.id.previewView);

//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
//        } else {
//            initBarcodeScanner();
//            startCamera();
//        }

        initBarcodeScanner();
        startCamera();

        ImageButton closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void initBarcodeScanner() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        barcodeScanner = BarcodeScanning.getClient(options);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), this::processImageForQRCode);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void processImageForQRCode(ImageProxy image) {
        try {
            @SuppressWarnings("UnsafeOptInUsageError")
            InputImage inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());

            barcodeScanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null) {
                                String scannedValue = rawValue.toLowerCase().trim();

                                if (scannedValue.equals(lastInvalidScannedValue)) {
                                    //Same invalid code scaned, skip processing
                                    break;
                                }

                                if(validLocations.contains(scannedValue)){
                                    // Pass the result back using Fragment Result API
                                    Toast.makeText(requireContext(), "QR Code Data: " + rawValue, Toast.LENGTH_SHORT).show();
                                    Bundle result = new Bundle();
                                    result.putString("qr_code_result", rawValue);
                                    getParentFragmentManager().setFragmentResult("qr_scan_result", result);

                                    // Dismiss the dialog
                                    dismiss();

                                    break;
                                }else {
                                    lastInvalidScannedValue = scannedValue;
                                    Toast.makeText(requireContext(), "Invalid QR Code scanned. Please try again: " + rawValue, Toast.LENGTH_SHORT).show();
                                }
                            }
                            break;
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure if necessary
                    })
                    .addOnCompleteListener(task -> image.close());
        } catch (Exception e) {
            image.close();
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Make dialog full screen
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        }
    }
}

