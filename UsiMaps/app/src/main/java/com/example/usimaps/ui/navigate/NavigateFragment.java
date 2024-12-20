package com.example.usimaps.ui.navigate;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.usimaps.BuildConfig;
import com.example.usimaps.DatabaseHelper;
import com.example.usimaps.QRCodeScannerDialogFragment;
import com.example.usimaps.R;
import com.example.usimaps.databinding.FragmentNavigateBinding;

//import graph class from the map package
import com.example.usimaps.map.Graph;
import com.example.usimaps.map.Vertex;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.example.usimaps.LocationAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import kotlin.Pair;

/**
 * Navigate Fragment for the navigation tab
 */
public class NavigateFragment extends Fragment {

    // View binding
    private FragmentNavigateBinding binding;

    // View pager
    private ViewPager2 viewPager;
    // Search bar and search view for the "from" location
    private SearchBar fromSearchBar, toSearchBar;
    private SearchView fromSearchView, toSearchView;
    // List of location suggestions
    private List<String> locationSuggestions;
    // Graph for navigation
    private Graph graph = new Graph();
    // Path and instructions
    private List<Vertex> path = new ArrayList<>();
    private List<String> instructions = new ArrayList<>();

    // Speech Recognition, Text to Speech, and AI Assistant
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isListening = false;
    private AlertDialog listeningDialog;
    private TextToSpeech textToSpeech;
    private GenerativeModel gm;
    private String llmprompt;

    // Permission launcher for camera and microphone
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestMicrophonePermissionLauncher;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the permission launchers
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openQRCodeScanner();
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to scan QR codes.", Toast.LENGTH_SHORT).show();
            }
        });
        requestMicrophonePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startVoiceInput();
            } else {
                Toast.makeText(requireContext(), "Microphone permission is required for voice recognition.", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(requireContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Set language
                    int result = textToSpeech.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(requireContext(), "TTS language not supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("Navigate Fragment","TTS initialization failed");
                }
            }
        });

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Optional: Provide feedback to the user
            }

            @Override
            public void onBeginningOfSpeech() {
                // Optional
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Optional
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Optional
            }

            @Override
            public void onEndOfSpeech() {
                isListening = false;
                if (listeningDialog != null && listeningDialog.isShowing()) {
                    listeningDialog.dismiss();
                }
            }

            @Override
            public void onError(int error) {
                isListening = false;
                if (listeningDialog != null && listeningDialog.isShowing()) {
                    listeningDialog.dismiss();
                }
                Log.e("Navigation Fragment","Speech recognition error");
            }

            @Override
            public void onResults(Bundle results) {
                isListening = false;
                if (listeningDialog != null && listeningDialog.isShowing()) {
                    listeningDialog.dismiss();
                }

                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0);

                    Toast.makeText(requireContext(), recognizedText, Toast.LENGTH_SHORT).show();

                    Log.i("Recognized Text:", recognizedText);

                    ListenableFuture<GenerateContentResponse> response = getLLMOutput(gm, recognizedText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Optional
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Optional
            }
        });

        // Set up the intent
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");// maybe put locale

        gm = new GenerativeModel("gemini-1.5-flash", BuildConfig.apiKey);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNavigateBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        DatabaseHelper db = new DatabaseHelper(requireContext());
        Graph loadedGraph = db.loadGraph("USI Campus EST");
        if (loadedGraph != null) {
            this.graph = loadedGraph;
            System.out.println("Graph loaded: " + graph.getMapName());
        } else {
            this.graph = graph.generateUSIMap();
        }

        fromSearchBar = binding.fromSearchBar;
        fromSearchView = binding.fromSearchView;

        toSearchBar = binding.toSearchBar;
        toSearchView = binding.toSearchView;

        this.locationSuggestions = graph.getSearchableNames();

        viewPager = binding.NavRouteViewPager;
        viewPager.setAdapter(new NavRouteAdapter(this));
        // disable swipe
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(2);
        showEmptyPathMessage(new ArrayList<>());

        // Set up the TabLayout
        TabLayout tabLayout = root.findViewById(R.id.NavRouteTabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Route");
            } else {
                tab.setText("Navigation");
            }
        }).attach();

        setupSearchBarAndView(fromSearchBar, fromSearchView, true);
        setupSearchBarAndView(toSearchBar, toSearchView, false);

        // Set up the Fragment Result Listener
        getChildFragmentManager().setFragmentResultListener("qr_scan_result", this, (requestKey, bundle) -> {
            String qrResult = bundle.getString("qr_code_result");
            if (qrResult != null) {
                fromSearchBar.setText(qrResult);
                fromSearchView.getEditText().setText(qrResult);
                fromLocationSelected(qrResult);
            }
        });

        //listener for QR code button inside the "from" search bar
        fromSearchBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_qr_scan) {
                checkAndRequestCameraPermission();
                return true;
            } else if (item.getItemId() == R.id.action_voice_input) {
                checkAndRequestMicrophonePermission();
                return true;
            }
            return false;
        });

        // init the LLM prompt for the first time
        this.resetLLMPrompt();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle args = getArguments();
        if (args != null) {
            String start = args.getString("start");
            String goal = args.getString("goal");
            // set the search bars
            fromSearchBar.setText(start);
            toSearchBar.setText(goal);
        }
        checkLocationsSelected(false);
    }

    //Speech Recognition

    /**
     * Start voice input
     */
    private void startVoiceInput() {
        if (!isListening) {
            speechRecognizer.startListening(speechRecognizerIntent);
            isListening = true;
            showListeningDialog();
        }
    }

    /**
     * Show the listening dialog
     */
    private void showListeningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Listening...")
                .setNegativeButton("Cancel", (dialog, which) -> {
                    speechRecognizer.cancel();
                    isListening = false;
                    dialog.dismiss();
                });
        listeningDialog = builder.create();
        listeningDialog.show();
    }

    /**
     * Check and request camera permission
     */
    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            openQRCodeScanner();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.CAMERA)) {
            // Explain to the user why the permission is needed
            new AlertDialog.Builder(requireContext())
                    .setTitle("Camera Permission Needed")
                    .setMessage("This app requires camera access to scan QR codes.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        } else {
            // Permission denied with "Don't Ask Again" or other reason
            new AlertDialog.Builder(requireContext())
                    .setTitle("Permission Denied")
                    .setMessage("Camera permission is required to scan QR codes. Please enable it in the app settings.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        // Open app settings
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }
    }

    /**
     * Check and request microphone permission
     */
    private void checkAndRequestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            startVoiceInput();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.RECORD_AUDIO)) {
            // Explain to the user why the permission is needed
            new AlertDialog.Builder(requireContext())
                    .setTitle("Microphone Permission Needed")
                    .setMessage("This app requires microphone access to recognize your voice.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        requestMicrophonePermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        } else {
            // Permission denied with "Don't Ask Again" or other reason
            new AlertDialog.Builder(requireContext())
                    .setTitle("Permission Denied")
                    .setMessage("Microphone permission is required for voice recognition. Please enable it in the app settings.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        // Open app settings
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }
    }

    /**
     * Speak the instructions
     */
    public void speakInstructions(String instruction){
        if(textToSpeech != null){
            textToSpeech.speak(instruction, TextToSpeech.QUEUE_FLUSH, null, "Sample ID");
        }
    }

    /**
     * Open the QR code scanner
     */
    private void openQRCodeScanner() {
        ArrayList<String> validLocations = new ArrayList<>(locationSuggestions);
        QRCodeScannerDialogFragment qrCodeScannerDialogFragment = QRCodeScannerDialogFragment.newInstance(validLocations);
        qrCodeScannerDialogFragment.show(getChildFragmentManager(), "qrCodeScanner");
    }

    /**
     * Update the path and instructions
     */
    private void updatePath() {

        // create a viewpager2 object
        FragmentStateAdapter adapter = (FragmentStateAdapter) this.viewPager.getAdapter();
        if (adapter != null) {
            List<Fragment> all_children = getChildFragmentManager().getFragments();

            for (int i = 0; i < adapter.getItemCount(); i++) {
                Fragment fragment = getChildFragmentManager().findFragmentByTag("f" + i);

                if (fragment instanceof RouteListFragment) {
                    ((RouteListFragment) fragment).updatePath(path, instructions);
                } else if (fragment instanceof NavigationCardsFragment) {
                    ((NavigationCardsFragment) fragment).updatePath(path, instructions);
                } else {
                    Log.i("Navigation Fragment", "Fragment not found");
                }
            }
        }
        Log.i("Navigation Fragment: ", "Path updated with " + path.size() + " vertices");
        showEmptyPathMessage(path);
    }

    /**
     * Show the empty path message if the path is empty, otherwise show the path
     */
    private void showEmptyPathMessage(List<Vertex> path) {
        if (path.isEmpty()) {
            binding.missingRouteIcon.setVisibility(View.VISIBLE);
            binding.emptyRouteText.setVisibility(View.VISIBLE);
            binding.NavRouteTabLayout.setVisibility(View.GONE);
        } else {
            binding.missingRouteIcon.setVisibility(View.GONE);
            binding.emptyRouteText.setVisibility(View.GONE);
            binding.NavRouteTabLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Update the route, then save it in the history database
     * @param newStart The new start location
     * @param newEnd The new end location
     */
    public void updateRoute(String newStart, String newEnd) {
        updateRoute(newStart, newEnd, true);
    }

    /**
     * Update the route, then save it in the history database
     * @param newStart The new start location
     * @param newEnd The new end location
     * @param saveHistory Whether to save the route in the history database
     */
    public void updateRoute(String newStart, String newEnd, boolean saveHistory) {
        Vertex startVertex = this.graph.getVertexByName(newStart);
        Vertex endVertex = graph.getVertexByName(newEnd);

        Pair<List<Vertex>, Double> shortestPath = graph.getShortestPath(startVertex, endVertex);
        List<Vertex> path = shortestPath.getFirst();
        double weight = shortestPath.getSecond();
        Pair<List<Vertex>, List<String>> pathInstructions = graph.toSimpleInstructions(path);
        this.path = pathInstructions.getFirst();
        this.instructions = pathInstructions.getSecond();

        updatePath();

        // save in the history db
        if (saveHistory) {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            dbHelper.saveHistory(newStart, newEnd);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        binding = null;

        if(textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    /**
     * Set up the search bar and search view
     * @param searchBar The search bar
     * @param searchView The search view
     * @param isFrom Whether the search bar is for the "from" location
     */
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
                                          int start, int count, int after) {
            }

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
            public void afterTextChanged(Editable s) {
            }
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

    /**
     * Handle the case when the "from" location is selected
     * @param location The selected location
     */
    private void fromLocationSelected(String location) {
        // Called when the "from" location is selected
        // Check if both locations are selected
        checkLocationsSelected();
    }

    /**
     * Handle the case when the "to" location is selected
     * @param location The selected location
     */
    private void toLocationSelected(String location) {
        // Called when the "to" location is selected
        // Check if both locations are selected
        checkLocationsSelected();
    }

    /**
     * Check if both locations are selected
     */
    private void checkLocationsSelected() {
        checkLocationsSelected(true);
    }

    /**
     * Check if both locations are selected
     * @param saveHistory Whether to save the route in the history database
     */
    private void checkLocationsSelected(boolean saveHistory) {
        String fromLocation = fromSearchBar.getText().toString();
        String toLocation = toSearchBar.getText().toString();
        if (!fromLocation.isEmpty() && !toLocation.isEmpty()) {
            // Both locations are selected
            updateRoute(fromLocation, toLocation, saveHistory);

            locationsSelected(fromLocation, toLocation);
        }
    }

    /**
     * Handle the case when both locations are selected
     * @param fromLocation The "from" location
     * @param toLocation The "to" location
     */
    private void locationsSelected(String fromLocation, String toLocation) {
        // Handle the case when both locations are selected
        Toast.makeText(requireContext(), "Both locations selected", Toast.LENGTH_SHORT).show();
    }


    /**
     * Reset the LLM prompt to the default value
     * This prompt is used to guide the AI assistant in extracting the source and destination locations
     * Every time the user starts a new conversation, the prompt is reset to the default value
     * During a conversation, the conversation history is appended to the prompt, so the AI assistant can keep track of the conversation
     * The goal is to extract the source and destination locations from the user input
     * The AI assistant will keep asking questions until it has extracted both locations
     */
    public void resetLLMPrompt(){
        llmprompt = "You are an AI assistant on a indoor navigation app. You are tasked with extracting the user current position and destination from a user input. " +
                "The user will describe their current location (source) and the desired destination (target) in natural language. " +
                "The possible locations are: ";
        for (String location : graph.getSearchableNames()) {
            llmprompt += location + ", ";
        }
        llmprompt = llmprompt.substring(0, llmprompt.length() - 2) + ". " +
                "Keep interacting with the user until you have extracted both locations. " +
                "If the input is unclear, investigate further by asking questions. " +
                "Once you have extracted both locations, respond only and only with 'Source Location: <source>, Destination Location: <destination>'." +
                "\nExamples: Input: 'I want to go from Room A to Room B.' Output: Source Location: Room A, Destination Location: Room B " +
                "Input: 'Take me from the library to the main hall.' Output: Source Location: Library, Destination Location: Main Hall Input: 'I'm starting at the cafeteria and heading to the science building.' " +
                "Output: Source Location: Cafeteria, Destination Location: Science Building Input: 'I just want to go somewhere.' Output: Unable to determine locations.";

        llmprompt += "\nPREVIOUS CONVERSATION:\n";

    }


    /**
     * Run the AI assistant and get the LLM output. The AI assistant will extract the source and destination locations from the user input.
     * The AI assistant will keep asking questions until it has extracted both locations.
     * @param gm The generative model
     * @param audio The audio input
     * @return The LLM output
     */
    private ListenableFuture<GenerateContentResponse> getLLMOutput(GenerativeModel gm, String audio){
        // Create a GenerativeModelFutures object
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        // Append the audio input to the LLM prompt
        llmprompt += "\nInput: '" + audio;
        // Pass the LLM prompt and audio input to the AI assistant
        Content content = new Content.Builder().addText(llmprompt).build();
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        // Add a callback to the response
        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String llmOutput = result.getText();
                        Log.i("LLM Output: ", llmOutput);

                        // Post the task back to the main thread
                        new Handler(Looper.getMainLooper()).post(() -> {
                            llmprompt += "\nOutput: '" + llmOutput;

                            if (llmOutput.contains("Source Location:") && llmOutput.contains("Destination Location:")) {
                                String[] parts = llmOutput.split("Source Location: ");
                                String source = parts[1].split(",")[0];
                                String destination = parts[1].split("Destination Location: ")[1].split(",")[0];
                                Log.i("LLM: ", "Source: " + source + " Destination: " + destination);

                                // trim source and destination
                                source = source.trim();
                                destination = destination.trim();
                                // double check if the locations are valid
                                if (!locationSuggestions.contains(source) || !locationSuggestions.contains(destination)) {
                                    Log.e("LLM:", "Invalid Locations");
                                    return;
                                }

                                fromSearchBar.setText(source);
                                fromSearchView.getEditText().setText(source);
                                fromLocationSelected(source);
                                toSearchBar.setText(destination);
                                toSearchView.getEditText().setText(destination);
                                toLocationSelected(destination);
                                resetLLMPrompt();
                            } else {
                                // speak the output
                                speakInstructions(llmOutput);

                                // wait till the speech is done
                                while(textToSpeech.isSpeaking()){
                                    // wait
                                }
                                // query again
                                startVoiceInput();
                            }

                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                        String llmOutput = t.getMessage();
                    }
                },
                executor);

        return response;
    }
}