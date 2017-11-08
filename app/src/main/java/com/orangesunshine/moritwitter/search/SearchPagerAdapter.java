package com.orangesunshine.moritwitter.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.orangesunshine.moritwitter.FollowList.FollowListMainFragment;
import com.simleman.moritwitter.R;

/**
 * Created by hayatomoritani on 7/13/17.
 */

public class SearchPagerAdapter extends FragmentPagerAdapter {

    private static final String tabNames[] = {"Users","Tweets"};
    public SearchPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return SearchPeopleFragment.newIntstance(0);
            case 1:
                return SearchPeopleFragment.newIntstance(1);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return tabNames[0];
            case 1:
                return tabNames[1];
            default:
                return "tab";
        }
    }
}
