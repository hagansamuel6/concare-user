package io.icode.concaregh.application.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    // Class Variables (Fragments and Titles)
    private ArrayList<Fragment> fragments;
    private ArrayList<String> titles;

    // Parameterized Constructor for class
    public ViewPagerAdapter(FragmentManager fm){
        super(fm);
        this.fragments = new ArrayList<>();
        this.titles = new ArrayList<>();
    }

    // returns each position of fragment
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    // returns size of fragment
    @Override
    public int getCount() {
        return fragments.size();
    }

    //method to add Fragment and their titles to the list
    public void addFragment(Fragment fragment, String title){
        fragments.add(fragment);
        titles.add(title);
    }

    // Ctrl + O
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
