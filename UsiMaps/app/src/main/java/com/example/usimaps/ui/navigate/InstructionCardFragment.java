package com.example.usimaps.ui.navigate;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.usimaps.R;
import com.example.usimaps.databinding.FragmentInstructionCardBinding;

import java.io.File;

/**
 * Instruction Card Fragment
 */
public class InstructionCardFragment extends Fragment {

    // View binding
    private FragmentInstructionCardBinding binding;
    // instruction text
    private final String instruction;
    // image paths array
    private final String imgPath;

    /**
     * Default constructor
     */
    public InstructionCardFragment() {
        this.instruction = "";
        this.imgPath = "";
    }

    /**
     * Constructor
     * @param instruction Instruction text
     * @param imgPath Image path
     */
    public InstructionCardFragment(String instruction, String imgPath) {
        this.instruction = instruction;
        this.imgPath = imgPath;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentInstructionCardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.instructionText.setText(instruction);


        // load image from file path
        System.out.println("imgPath: " + imgPath);
        File imgFile = new File(imgPath);
        if(imgFile.exists()){
            binding.instructionImage.setImageURI(Uri.parse(imgPath));
        } else {
            // remove image
            binding.instructionImage.setVisibility(View.GONE);
        }
        if (instruction.contains("stairs"))
            binding.instructionIcon.setImageResource(R.drawable.stairs);
        else if (instruction.contains("left"))
            binding.instructionIcon.setImageResource(R.drawable.ic_turn_left);
        else if (instruction.contains("right"))
            binding.instructionIcon.setImageResource(R.drawable.ic_turn_right);
        else
            binding.instructionIcon.setImageResource(R.drawable.map_pin);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

