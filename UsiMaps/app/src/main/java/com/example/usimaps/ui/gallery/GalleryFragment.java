package com.example.usimaps.ui.gallery;

import android.os.Bundle;
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

import com.example.usimaps.R;
import com.example.usimaps.RecyclerViewAdapter;
import com.example.usimaps.ViewPagerAdapter;
import com.example.usimaps.databinding.FragmentGalleryBinding;

//import graph class from the map package
import com.example.usimaps.map.Graph;
import com.example.usimaps.map.Vertex;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import kotlin.Pair;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;

    private ViewPager2 viewPager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        Graph graph = new Graph().generateUSIMap();
        String start = "D0:04";
        String end = "D1:15";
        Vertex startVertex = graph.getVertexByName(start);
        Vertex endVertex = graph.getVertexByName(end);


        // get the shortest path from start to end
        Pair<List<Vertex>, Double> shortestPath = graph.getShortestPath(startVertex, endVertex);
        List<Vertex> path = shortestPath.getFirst();
        double weight = shortestPath.getSecond();

        // simplify the path
        Pair<List<Vertex>, List<String>> pathInstructions = graph.toSimpleInstructions(path);
        path = pathInstructions.getFirst();
        List<String> instructions = pathInstructions.getSecond();

        // create a viewpager2 object
//        createViewPager(root, path, instructions);
//        createRecyclerView(root, path, instructions);


        viewPager = root.findViewById(R.id.NavRouteViewPager);
        List<Vertex> finalPath = path;
        viewPager.setAdapter(new FragmentStateAdapter(this) {

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return new RouteListFragment(finalPath, instructions);
                } else {
                    return new NavigationCardsFragment(finalPath, instructions);
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }

        });

        // disable swipe
        viewPager.setUserInputEnabled(false);



        TabLayout tabLayout = root.findViewById(R.id.NavRouteTabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Route");
            } else {
                tab.setText("Navigation");
            }
        }).attach();

        setButtonListeners(root);

        return root;

    }

    private void updatePath(List<Vertex> path, List<String> instructions) {
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



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    // define button click listener for the next and previous buttons
    private void setButtonListeners(View root) {
        Button nextButton = root.findViewById(R.id.next_button);
        Button previousButton = root.findViewById(R.id.prev_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1);
                Graph graph = new Graph().generateUSIMap();
                // list of vertices
                List<String> classes = graph.getSearchableNames();
                // get random
                String newStart = classes.get((int) (Math.random() * classes.size()));
                String newEnd = classes.get((int) (Math.random() * classes.size()));

                Vertex startVertex = graph.getVertexByName(newStart);
                Vertex endVertex = graph.getVertexByName(newEnd);

                Pair<List<Vertex>, Double> shortestPath = graph.getShortestPath(startVertex, endVertex);
                List<Vertex> path = shortestPath.getFirst();
                double weight = shortestPath.getSecond();
                Pair<List<Vertex>, List<String>> pathInstructions = graph.toSimpleInstructions(path);
                path = pathInstructions.getFirst();
                List<String> instructions = pathInstructions.getSecond();

                // update the path
                updatePath(path, instructions);


            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}