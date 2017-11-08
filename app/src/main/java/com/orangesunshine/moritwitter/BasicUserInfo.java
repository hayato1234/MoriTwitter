package com.orangesunshine.moritwitter;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by hayatomoritani on 12/6/16.
 */

public class BasicUserInfo{

    private String screenName;
    private String nameB;
    private String iconURL;
    private long time;

    private int totalTweet;
    private int difference;

    private int isDisabled;

    public int getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(int isDisabled) {
        this.isDisabled = isDisabled;
    }

    public int getDifference() {
        return difference;
    }

    public void setDifference(int difference) {
        this.difference = difference;
    }

    public int getTotalTweet() {
        return totalTweet;
    }

    public void setTotalTweet(int totalTweet) {
        this.totalTweet = totalTweet;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /*public Boolean getNew() {
        return isNew;
    }

    public void setNew(Boolean aNew) {
        isNew = aNew;
    }*/

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getNameB() {
        return nameB;
    }

    public void setNameB(String name) {
        this.nameB = name;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

}

