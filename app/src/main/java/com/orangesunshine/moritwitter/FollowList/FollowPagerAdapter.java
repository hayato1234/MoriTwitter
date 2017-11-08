package com.orangesunshine.moritwitter.FollowList;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.orangesunshine.moritwitter.AppUserPage.UserPageFragement;
import com.orangesunshine.moritwitter.TimeLineTab.TimeLineTabFragment;
import com.orangesunshine.moritwitter.mention_tab.MentionTabFragment;

import java.util.HashMap;

/**
 * Created by hayatomoritani on 5/10/17.
 */

public class FollowPagerAdapter extends FragmentPagerAdapter {


    FollowPagerAdapter(FragmentManager fm, String name){
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new FollowListMainFragment();
            case 1:
                return new TimeLineTabFragment();
            case 2:
                return new MentionTabFragment();
            default:
                return UserPageFragement.newInstance("");
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return "";
    }



    //for calling tab fragment method from main activity
    //used in scroll up of timeline
    private HashMap<Integer, Fragment> mPageReferenceMap = new HashMap<>();
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mPageReferenceMap.put(position, fragment);
        return fragment;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        mPageReferenceMap.remove(position);
    }
    public Fragment getFragment(int key) {
        return mPageReferenceMap.get(key);
    }
}
