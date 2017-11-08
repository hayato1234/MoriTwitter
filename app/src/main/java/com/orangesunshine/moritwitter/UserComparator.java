package com.orangesunshine.moritwitter;

import java.util.Comparator;

public class UserComparator implements Comparator<BasicUserInfo> {


    @Override
    public int compare(BasicUserInfo user1, BasicUserInfo user2) {
        return user1.getDifference()<user2.getDifference()?1:-1;
    }
}
