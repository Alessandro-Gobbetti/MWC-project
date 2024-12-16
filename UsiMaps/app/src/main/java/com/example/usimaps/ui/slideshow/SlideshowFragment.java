package com.example.usimaps.ui.slideshow;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.usimaps.DatabaseHelper;
import com.example.usimaps.databinding.FragmentSlideshowBinding;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.Pair;
import kotlin.Triple;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;

    private Button ttsButton;

    private TextToSpeech textToSpeech;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();




        // Fetch history data
        List<Triple<String, String, String>> history = getHistory();

        // Add RouteHistoryListFragment
        RouteHistoryListFragment routeHistoryListFragment = new RouteHistoryListFragment(history);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(binding.historyContainer.getId(), routeHistoryListFragment);
        transaction.commit();

        if (!history.isEmpty()) {
            // mark the missing history text view as gone
            ImageView missingHistory = binding.missingHistoryIcon;
            missingHistory.setVisibility(View.GONE);
            TextView missingHistoryText = binding.emptyHistoryText;
            missingHistoryText.setVisibility(View.GONE);
        }


        ttsButton = binding.TTSButton;
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
                        ttsButton.setOnClickListener(v -> {
                            // Speak out the instruction
                            String instruction = "Turn left in 100 meters onto Main Street.";
                            speakInstructions("Testing speech");
                        });
                        Toast.makeText(requireContext(), "TTS OKAY", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "TTS initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return root;
    }

    public void speakInstructions(String instruction){
        if(textToSpeech != null){
            Toast.makeText(requireContext(), "TTS Speaking", Toast.LENGTH_SHORT).show();
            textToSpeech.speak(instruction.trim(), TextToSpeech.QUEUE_FLUSH, null, "Sample ID");
        }else {
            Toast.makeText(requireContext(), "TTS is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private List<Triple<String, String, String>> getHistory() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_DATE,
                DatabaseHelper.COLUMN_START,
                DatabaseHelper.COLUMN_GOAL
        };
        String sortOrder = DatabaseHelper.COLUMN_ID + " DESC";
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_HISTORY,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        List<Triple<String, String, String>> history = new ArrayList<>();
        while (cursor.moveToNext()) {
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
            String start = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START));
            String goal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GOAL));
            history.add(new Triple<>(date, start, goal));

        }
        cursor.close();
        db.close();
        return history;
    }

}