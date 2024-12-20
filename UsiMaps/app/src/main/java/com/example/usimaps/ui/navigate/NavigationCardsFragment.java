package com.example.usimaps.ui.navigate;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.usimaps.R;
import com.example.usimaps.databinding.FragmentNavigationCardsBinding;
import com.example.usimaps.map.Vertex;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Displays the navigation cards
 */
public class NavigationCardsFragment extends Fragment {
    // Path and instructions
    private List<Vertex> path;
    private List<String> instructions;
    // View binding
    private FragmentNavigationCardsBinding binding;
    // Viewpager2 and TabLayout
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;
    // Text to speech
    private TextToSpeech textToSpeech;

    /**
     * Default constructor
     */
    public NavigationCardsFragment() {
        // Required empty public constructor
        this.path = new ArrayList<>();
        this.instructions = new ArrayList<>();
    }

    /**
     * Constructor
     * @param path Path
     * @param instructions Instructions
     */
    public NavigationCardsFragment(List<Vertex> path, List<String> instructions) {
        this.path = path;
        this.instructions = instructions;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNavigationCardsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewPager2 = root.findViewById(R.id.viewPager);
        tabLayout = root.findViewById(R.id.tabLayout);

        // add the cards to the viewpager2 object
        this.adapter = new ViewPagerAdapter(getActivity(), path, instructions);
        viewPager2.setAdapter(adapter);

        // set the tablayout with the viewpager2
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {}
        ).attach();

        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Set language
                int result = textToSpeech.setLanguage(Locale.getDefault());
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(requireContext(), "TTS language not supported", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "TTS initialization failed", Toast.LENGTH_SHORT).show();
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position >= 0 && position < instructions.size()) {
                    String instruction = instructions.get(position);
                    speakInstruction(instruction);
                }
            }
        });

        return root;
    }

    /**
     * Speak the instruction
     * @param instruction Instruction
     */
    private void speakInstruction(String instruction) {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.speak(instruction, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    /**
     * Update the path
     * @param path Path
     * @param instructions Instructions
     */
    public void updatePath(List<Vertex> path, List<String> instructions) {
        this.path = path;
        this.instructions = instructions;

        adapter = new ViewPagerAdapter(getActivity(), path, instructions);
        viewPager2.setAdapter(adapter);

        // set the tablayout with the viewpager2
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {}
        ).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        textToSpeech.stop();
        textToSpeech.shutdown();
    }
}