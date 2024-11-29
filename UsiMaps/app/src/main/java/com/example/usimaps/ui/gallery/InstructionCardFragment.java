package com.example.usimaps.ui.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.usimaps.R;
import com.example.usimaps.databinding.FragmentInstructionCardBinding;

import java.io.File;


public class InstructionCardFragment extends Fragment {

    private FragmentInstructionCardBinding binding;
    private final String instruction;

    // image paths array
    private final String imgPath;

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
        File imgFile = new File(imgPath);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            binding.instructionImage.setImageBitmap(myBitmap);
        } else {
            // remove image
            binding.instructionImage.setVisibility(View.GONE);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

