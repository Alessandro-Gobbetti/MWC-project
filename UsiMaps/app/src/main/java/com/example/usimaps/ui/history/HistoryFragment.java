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

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;

    private Button ttsButton;

    private TextToSpeech textToSpeech;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HistoryViewModel historyViewModel =
                new ViewModelProvider(this).get(HistoryViewModel.class);

        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();




        // Fetch history data
        List<Triple<String, String, String>> history = getHistory();

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