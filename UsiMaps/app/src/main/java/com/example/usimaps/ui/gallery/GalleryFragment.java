package com.example.usimaps.ui.gallery;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.usimaps.DatabaseHelper;
import com.example.usimaps.QRCodeScannerDialogFragment;
import com.example.usimaps.R;
import com.example.usimaps.RecyclerViewAdapter;
import com.example.usimaps.ViewPagerAdapter;
import com.example.usimaps.databinding.FragmentGalleryBinding;

//import graph class from the map package
import com.example.usimaps.map.Graph;
import com.example.usimaps.map.Vertex;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.example.usimaps.LocationAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import kotlin.Pair;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;

    private ViewPager2 viewPager;

    private SearchBar fromSearchBar, toSearchBar;
    private SearchView fromSearchView, toSearchView;

    private List<String> locationSuggestions;
    private String selectedFromLocation, selectedToLocation;

    private Graph graph = new Graph();

    private List<Vertex> path = new ArrayList<>();
    private List<String> instructions = new ArrayList<>();

    // Speech Recognition
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isListening = false;
    private AlertDialog listeningDialog;

    private TextToSpeech textToSpeech;
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textToSpeech = new TextToSpeech(requireContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Set language
                    int result = textToSpeech.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(requireContext(), "TTS language not supported", Toast.LENGTH_SHORT).show();
                    } else {
                        // TTS is ready, set up the button listener
//                        speakButton.setOnClickListener(v -> {
//                            // Speak out the instruction
//                            String instruction = "Turn left in 100 meters onto Main Street.";
//                            speakInstructions("Testing speech");
//                            textToSpeech.speak(instruction, TextToSpeech.QUEUE_FLUSH, null, "InstructionID");
//                        });
                        Toast.makeText(requireContext(), "TTS OKAY", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "TTS initialization failed", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(requireContext(), "Speech recognition error", Toast.LENGTH_SHORT).show();
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

                    System.out.println("RECOGNIZED TEXT" + recognizedText);
                    Toast.makeText(requireContext(), recognizedText, Toast.LENGTH_SHORT).show();

//                    fromSearchBar.setText(recognizedText);
//                    fromSearchView.getEditText().setText(recognizedText);
//                    fromLocationSelected(recognizedText);
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
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        byte[] bytegraph = Graph.serialize(graph);
//        System.out.println("Bytegraph: " + Arrays.toString(bytegraph));
//        Graph graph_read = Graph.deserialize(bytegraph);
//        System.out.println("Graph read: " + graph_read);
//        graph_read.print();
//        System.out.println("______________");

//        saveGraph();
        this.graph = loadGraph("USI Campus EST");
        System.out.println("LOAD______________");
        System.out.println("Names: " + getMapNames());
        //
        fromSearchBar = binding.fromSearchBar;
        fromSearchView = binding.fromSearchView;

        toSearchBar = binding.toSearchBar;
        toSearchView = binding.toSearchView;

        this.locationSuggestions = graph.getSearchableNames();


        viewPager = binding.NavRouteViewPager;
        viewPager.setAdapter(new FragmentStateAdapter(this) {

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return new RouteListFragment();
                } else {
                    return new NavigationCardsFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }

        });

        // disable swipe
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(2);
        showEmptyPathMessage(new ArrayList<>());


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
                openQRCodeScanner();
                return true;
            } else if (item.getItemId() == R.id.action_voice_input) {
                Toast.makeText(requireContext(), "Mic clicked!", Toast.LENGTH_SHORT).show();
                startVoiceInput();
                return true;
            }
            return false;
        });

        // Retrieve the route data from the arguments
        Bundle args = getArguments();
        if (args != null) {
            String start = args.getString("start");
            String goal = args.getString("goal");
            // set the search bars
            fromSearchBar.setText(start);
            toSearchBar.setText(goal);
            // Display the route
            checkLocationsSelected();
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the title
        System.out.println("RESUMED");

        Bundle args = getArguments();
        if (args != null) {
            String start = args.getString("start");
            String goal = args.getString("goal");
            // set the search bars
            fromSearchBar.setText(start);
            toSearchBar.setText(goal);
        }
        checkLocationsSelected();
    }

    //Speech Recognition

    private void startVoiceInput() {
        if (!isListening) {
            speechRecognizer.startListening(speechRecognizerIntent);
            isListening = true;
            showListeningDialog();
        }
    }

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

    private void openQRCodeScanner() {
        ArrayList<String> validLocations = new ArrayList<>(locationSuggestions);
        QRCodeScannerDialogFragment qrCodeScannerDialogFragment = QRCodeScannerDialogFragment.newInstance(validLocations);
        qrCodeScannerDialogFragment.show(getChildFragmentManager(), "qrCodeScanner");
    }

    private void updatePath() {
        // create a viewpager2 object
        FragmentStateAdapter adapter = (FragmentStateAdapter) this.viewPager.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                Fragment fragment = getChildFragmentManager().findFragmentByTag("f" + i);
                if (fragment instanceof RouteListFragment) {
                    ((RouteListFragment) fragment).updatePath(path, instructions);
                } else if (fragment instanceof NavigationCardsFragment) {
                    ((NavigationCardsFragment) fragment).updatePath(path, instructions);
                }
            }
        }
        System.out.println("Path updated with " + path.size() + " vertices");

        showEmptyPathMessage(path);
    }

    private void showEmptyPathMessage(List<Vertex> path) {
        System.out.println("Path size: " + path.size() + " vertices" + path.isEmpty());
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

    public void speakInstructions(String instruction){
        if(textToSpeech != null){
            Toast.makeText(requireContext(), "TTS Speaking", Toast.LENGTH_SHORT).show();
            textToSpeech.speak(instruction, TextToSpeech.QUEUE_FLUSH, null, "Sample ID");
        }else {
            Toast.makeText(requireContext(), "TTS is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void createViewPager(View root, List<Vertex> path, List<String> instructions) {
//        // create a viewpager2 object
//        ViewPager2 viewPager2 = root.findViewById(R.id.viewPager);
//        TabLayout tabLayout = root.findViewById(R.id.tabLayout);
//
//        // add the cards to the viewpager2 object
//        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity(), path, instructions);
//        viewPager2.setAdapter(adapter);
//
//        // set the tablayout with the viewpager2
//        new TabLayoutMediator(tabLayout, viewPager2,
//            (tab, position) -> {}
//        ).attach();
//
//        // set the button listeners
//        setButtonListeners(root, viewPager2);
    }

    // use a recyclerview to display the instructions
    private void createRecyclerView(View root, List<Vertex> path, List<String> instructions) {
        // create a recyclerview object
//        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
//
//        // create a recyclerview adapter
//        RecyclerViewAdapter adapter = new RecyclerViewAdapter(path, instructions);
//
//        // set the layout manager
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//
//        // set the adapter
//        recyclerView.setAdapter(adapter);
    }

    public void updateRoute(String newStart, String newEnd) {
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
        saveHistory(newStart, newEnd);
    }

    private void saveHistory(String start, String end) {
        // save the start and end locations in the history database
         DatabaseHelper dbHelper = new DatabaseHelper(getContext());
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         ContentValues values = new ContentValues();
         // date
         Date date = new Date();
         // To get local formatting use getDateInstance(), getDateTimeInstance(), or getTimeInstance(), or use new SimpleDateFormat(String template, Locale locale
         SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
         String strDate = formatter.format(date);
         values.put(DatabaseHelper.COLUMN_DATE, strDate);
         values.put(DatabaseHelper.COLUMN_START, start);
         values.put(DatabaseHelper.COLUMN_GOAL, end);
         long newRowId = db.insert(DatabaseHelper.TABLE_HISTORY, null, values);
         db.close();
    }

    private void saveGraph() {
        // save the graph in the database, if already exists, update
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String mapName = graph.getMapName();
        byte[] bytegraph = Graph.serialize(graph);
        values.put(DatabaseHelper.COLUMN_MAP_NAME, mapName);
        values.put(DatabaseHelper.COLUMN_MAP_OBJECT, bytegraph);

        // Check if the map already exists
        String selection = DatabaseHelper.COLUMN_MAP_NAME + " = ?";
        String[] selectionArgs = { mapName };

        int count = db.update(DatabaseHelper.TABLE_MAPS, values, selection, selectionArgs);

        // If the map does not exist, insert it
        if (count == 0) {
            db.insert(DatabaseHelper.TABLE_MAPS, null, values);
        }

        db.close();
    }

    private Graph loadGraph(String name) {
        // load the graph from the database
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_MAP_NAME,
                DatabaseHelper.COLUMN_MAP_OBJECT
        };
        String selection = DatabaseHelper.COLUMN_MAP_NAME + " = ?";
        String[] selectionArgs = {name};
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MAPS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        Graph graph = null;
        if (cursor.moveToNext()) {
            byte[] bytegraph = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MAP_OBJECT));
            graph = Graph.deserialize(bytegraph);
        }
        cursor.close();
        db.close();
        return graph;
    }

    public List<String> getMapNames() {
        // get the names of the maps in the database
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_MAP_NAME
        };
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MAPS,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        List<String> mapNames = new ArrayList<>();
        while (cursor.moveToNext()) {
            String mapName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MAP_NAME));
            mapNames.add(mapName);
        }
        cursor.close();
        db.close();
        return mapNames;
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

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        // Save the path and instructions
//        outState.putParcelableArrayList("path", new ArrayList<>(path));
//        outState.putStringArrayList("instructions", new ArrayList<>(instructions));
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
//    }


    // define button click listener for the next and previous buttons
//    private void setButtonListeners(View root) {
//        Button nextButton = root.findViewById(R.id.next_button);
//        Button previousButton = root.findViewById(R.id.prev_button);
//
//        nextButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1);
//                Graph graph = new Graph().generateUSIMap();
//                // list of vertices
//                List<String> classes = graph.getSearchableNames();
//                // get random
//                String newStart = classes.get((int) (Math.random() * classes.size()));
//                String newEnd = classes.get((int) (Math.random() * classes.size()));
//
//                Vertex startVertex = graph.getVertexByName(newStart);
//                Vertex endVertex = graph.getVertexByName(newEnd);
//
//                Pair<List<Vertex>, Double> shortestPath = graph.getShortestPath(startVertex, endVertex);
//                List<Vertex> path = shortestPath.getFirst();
//                double weight = shortestPath.getSecond();
//                Pair<List<Vertex>, List<String>> pathInstructions = graph.toSimpleInstructions(path);
//                path = pathInstructions.getFirst();
//                List<String> instructions = pathInstructions.getSecond();
//
//                // update the path
//                updatePath(path, instructions);
//
//
//            }
//        });
//
//        previousButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//    }

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

    private void fromLocationSelected(String location) {
        // Called when the "from" location is selected
        Toast.makeText(requireContext(), "From location selected", Toast.LENGTH_SHORT).show();
        selectedFromLocation = location;

        // Check if both locations are selected
        checkLocationsSelected();
    }

    private void toLocationSelected(String location) {
        // Called when the "to" location is selected
        Toast.makeText(requireContext(), "To location selected", Toast.LENGTH_SHORT).show();
        selectedToLocation = location;

        // Check if both locations are selected
        checkLocationsSelected();
    }

    private void checkLocationsSelected() {
        String fromLocation = fromSearchBar.getText().toString();
        String toLocation = toSearchBar.getText().toString();
        if (!fromLocation.isEmpty() && !toLocation.isEmpty()) {
            // Both locations are selected
            updateRoute(fromLocation, toLocation);

            locationsSelected(fromLocation, toLocation);
        }
    }

    private void locationsSelected(String fromLocation, String toLocation) {
        // Handle the case when both locations are selected
        Toast.makeText(requireContext(), "Both locations selected", Toast.LENGTH_SHORT).show();
    }
}