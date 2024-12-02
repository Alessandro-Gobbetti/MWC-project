package com.example.usimaps.ui.gallery;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.usimaps.R;
import com.example.usimaps.ViewPagerAdapter;
import com.example.usimaps.databinding.FragmentNavigationCardsBinding;
import com.example.usimaps.map.Vertex;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

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
    
    public NavigationCardsFragment() {
        // Required empty public constructor
        this.path = new ArrayList<>();
        this.instructions = new ArrayList<>();
    }


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

        // set the button listeners
//        setButtonListeners(root, viewPager2);

        return root;
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



    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}