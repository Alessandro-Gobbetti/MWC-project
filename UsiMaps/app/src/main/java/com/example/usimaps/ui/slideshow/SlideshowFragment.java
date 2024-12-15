package com.example.usimaps.ui.slideshow;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.usimaps.databinding.FragmentSlideshowBinding;

import java.util.Locale;

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

        final TextView textView = binding.textSlideshow;
        slideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

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
}