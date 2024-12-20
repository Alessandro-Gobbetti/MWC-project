package com.example.usimaps;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.usimaps.map.Vertex;
import com.example.usimaps.ui.navigate.InstructionCardFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private List<Vertex> path;
    private List<Fragment> fragmentList = new ArrayList();
    private List<String> instructions = new ArrayList();

    public ViewPagerAdapter(FragmentActivity fragmentActivity, List<Vertex> path, List<String> instructions) {
        super(fragmentActivity);
        this.path = path;
        this.instructions = instructions;
        System.out.println("-----path: " + path);
        System.out.println("-----instructions: " + instructions);

        // populate the fragmentList with the path
        for (int i = 0; i < path.size(); i++) {
            fragmentList.add(new InstructionCardFragment(instructions.get(i), path.get(i).getImagePath()));
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return path.size();
    }

    public String getPageTitle(int position) {
        return path.get(position).getName();
    }

    public void addFragment(Fragment fragment) {
        fragmentList.add(fragment);
    }

    public void removeFragment(Fragment fragment) {
        fragmentList.remove(fragment);
    }

    public void clearFragments() {
        fragmentList.clear();
    }

    public List<Fragment> getFragments() {
        return fragmentList;
    }

    public void updatePath(List<Vertex> path, List<String> instructions) {
        this.path = path;
        this.instructions = instructions;
    }


}
