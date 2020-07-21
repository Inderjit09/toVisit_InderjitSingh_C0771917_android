package com.tovisit_inderjitsingh_c0771917_android.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import com.tovisit_inderjitsingh_c0771917_android.views.fragments.DreamPlacesFragment;
import com.tovisit_inderjitsingh_c0771917_android.views.fragments.MapsFragment;

public class HomeViewPagerAdapter extends FragmentStatePagerAdapter {
    private int tabsCount;

    public HomeViewPagerAdapter(FragmentManager supportFragmentManager, int tabsCount) {
        super(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.tabsCount = tabsCount;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        switch (i) {
            case 0: {
                fragment = new MapsFragment();
                break;
            }
            case 1: {
                fragment = new DreamPlacesFragment();
                break;
            }
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return tabsCount;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return HomeViewPagerAdapter.POSITION_NONE;
    }

}
