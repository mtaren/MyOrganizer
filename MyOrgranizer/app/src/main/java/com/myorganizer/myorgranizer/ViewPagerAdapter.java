package com.myorganizer.myorgranizer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;


import com.myorganizer.myorgranizer.HomeFragment;
import com.myorganizer.myorgranizer.NotificationFragment;
import com.myorganizer.myorgranizer.searchFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 3;
    // Tab Titles
    private String tabtitles[] = new String[] { "Home", "Search", "Notifications" };
    Context context;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

            // Open FragmentTab1.java
            case 0:
                HomeFragment fragmenttab1 = new HomeFragment();
                return fragmenttab1;

            // Open FragmentTab2.java
            case 1:
                searchFragment fragmenttab2 = new searchFragment();
                return fragmenttab2;

            // Open FragmentTab3.java
            case 2:
                NotificationFragment fragmenttab3 = new NotificationFragment();
                return fragmenttab3;
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabtitles[position];
    }
}
