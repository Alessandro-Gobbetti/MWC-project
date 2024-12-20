package com.example.usimaps.ui.navigate;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class NavRouteAdapter extends FragmentStateAdapter {

    private RouteListFragment routeListFragment;
    private NavigationCardsFragment navigationCardsFragment;

    public NavRouteAdapter(NavigateFragment fragmentActivity) {
        super(fragmentActivity);
        this.routeListFragment = new RouteListFragment();
        this.navigationCardsFragment = new NavigationCardsFragment();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new RouteListFragment();
        } else {
            return new NavigationCardsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
