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
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NavigationCardsFragment extends Fragment {

    private List<Vertex> path;
    private List<String> instructions;

    private FragmentNavigationCardsBinding binding;

    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;

    private TextToSpeech textToSpeech;
    
    public NavigationCardsFragment() {
        // Required empty public constructor
        this.path = new ArrayList<>();
        this.instructions = new ArrayList<>();
    }

    // TODO: Improve how parameters are passed, use Bundle instead
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
                } else {
                    Toast.makeText(requireContext(), "TTS OKAY", Toast.LENGTH_SHORT).show();
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




        // set the button listeners
//        setButtonListeners(root, viewPager2);

        return root;
    }

    private void speakInstruction(String instruction) {
        //TODO: Think about the second part of this condition
        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
            textToSpeech.speak(instruction, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

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