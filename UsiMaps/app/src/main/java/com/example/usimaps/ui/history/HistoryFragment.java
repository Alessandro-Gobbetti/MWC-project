package com.example.usimaps.ui.history;

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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.usimaps.DatabaseHelper;
import com.example.usimaps.databinding.FragmentHistoryBinding;

import java.util.ArrayList;
import java.util.List;

import kotlin.Triple;

/**
 * Fragment for the history tab
 */
public class HistoryFragment extends Fragment {

    // View binding
    private FragmentHistoryBinding binding;
    // Text to speech button
    private Button ttsButton;
    // Text to speech object
    private TextToSpeech textToSpeech;

    /**
     * Create the view
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Fetch history data
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        List<Triple<String, String, String>> history = dbHelper.getHistory();

        // Add RouteHistoryListFragment
        RouteHistoryListFragment routeHistoryListFragment = new RouteHistoryListFragment(history);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(binding.historyContainer.getId(), routeHistoryListFragment);
        transaction.commitAllowingStateLoss();

        if (!history.isEmpty()) {
            // mark the missing history text view as gone
            ImageView missingHistory = binding.missingHistoryIcon;
            missingHistory.setVisibility(View.GONE);
            TextView missingHistoryText = binding.emptyHistoryText;
            missingHistoryText.setVisibility(View.GONE);
        }

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}