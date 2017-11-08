package com.orangesunshine.moritwitter;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hayatomoritani on 10/28/16.
 */

public class Tweet implements Parcelable{

    private String userName;
    private String screenName;
    private String reTweetedByName;
    private String content;
    private String prof_image;
    private Date time;
    private ArrayList<String> media_images;
    private String media_vidoes;
    private String media_gifs;
    private String youtubeId;
    private String instaUrl;
    private String repliesNum;
    private String retweetNum;
    private boolean isReTweeted;
    private boolean isRetweet;
    private boolean isFaved;
    private String likeNum;
    private long tweetId;
    private long retweetedId;
    private boolean isQuoted;
    private long quoteId;
    private String QuoteName;
    private String QuoteContent;

    public Tweet(){}

    protected Tweet(Parcel in) {
        userName = in.readString();
        screenName = in.readString();
        reTweetedByName = in.readString();
        content = in.readString();
        prof_image = in.readString();
        try {
            time = new SimpleDateFormat("YYYYMMDDTHHMMSS", Locale.US).parse(in.readString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        media_images = in.createStringArrayList();
        media_vidoes = in.readString();
        media_gifs = in.readString();
        youtubeId = in.readString();
        instaUrl = in.readString();
        repliesNum = in.readString();
        retweetNum = in.readString();
        isReTweeted = in.readByte() != 0;
        isRetweet = in.readByte() != 0;
        isFaved = in.readByte() != 0;
        likeNum = in.readString();
        tweetId = in.readLong();
        retweetedId = in.readLong();
        isQuoted = in.readByte() != 0;
        quoteId = in.readLong();
        QuoteName = in.readString();
        QuoteContent = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userName);
        dest.writeString(screenName);
        dest.writeString(reTweetedByName);
        dest.writeString(content);
        dest.writeString(prof_image);
        dest.writeString(time.toString());
        dest.writeStringList(media_images);
        dest.writeString(media_vidoes);
        dest.writeString(media_gifs);
        dest.writeString(youtubeId);
        dest.writeString(instaUrl);
        dest.writeString(repliesNum);
        dest.writeString(retweetNum);
        dest.writeByte((byte)(isReTweeted?1:0));
        dest.writeByte((byte)(isRetweet?1:0));
        dest.writeByte((byte)(isFaved?1:0));
        dest.writeString(likeNum);
        dest.writeLong(tweetId);
        dest.writeLong(retweetedId);
        dest.writeByte((byte)(isQuoted?1:0));
        dest.writeLong(quoteId);
        dest.writeString(QuoteName);
        dest.writeString(QuoteContent);
    }

    public long getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(long quoteId) {
        this.quoteId = quoteId;
    }

    public boolean isQuoted() {
        return isQuoted;
    }

    public void setQuoted(boolean quoted) {
        isQuoted = quoted;
    }

    public String getQuoteName() {
        return QuoteName;
    }

    public void setQuoteName(String quoteName) {
        QuoteName = quoteName;
    }

    public String getQuoteContent() {
        return QuoteContent;
    }

    public void setQuoteContent(String quoteContent) {
        QuoteContent = quoteContent;
    }

    public boolean isFaved() {
        return isFaved;
    }

    public void setFaved(boolean faved) {
        isFaved = faved;
    }

    public long getRetweetedId() {
        return retweetedId;
    }

    public void setRetweetedId(long retweetedId) {
        this.retweetedId = retweetedId;
    }

    public boolean isReTweeted() {
        return isReTweeted;
    }

    public void setReTweeted(boolean reTweeted) {
        isReTweeted = reTweeted;
    }

    public String getReplyNum() {
        return repliesNum;
    }

    public void setReplyNum(String repliesNum) {
        this.repliesNum = repliesNum;
    }

    public String getRetweetNum() {
        return retweetNum;
    }

    public void setRetweetNum(String retweetNum) {
        this.retweetNum = retweetNum;
    }

    public String getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(String likeNum) {
        this.likeNum = likeNum;
    }

    public boolean isRetweet() {
        return isRetweet;
    }

    public void setRetweet(boolean retweet) {
        isRetweet = retweet;
    }

    public String getInstaUrl() {
        return instaUrl;
    }

    public void setInstaUrl(String instaUrl) {
        this.instaUrl = instaUrl;
    }



    public String getYoutubeId() {
        return youtubeId;
    }

    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }

    public void setMedia_vidoes(String media_vidoes) {
        this.media_vidoes = media_vidoes;
    }

    public String getMedia_vidoes() {
        return media_vidoes;
    }



    public String getMedia_gifs() {
        return media_gifs;
    }

    public void setMedia_gifs(String media_gifs) {
        this.media_gifs = media_gifs;
    }

    public long getTweetId() {
        return tweetId;
    }

    public void setTweetId(long tweetId) {
        this.tweetId = tweetId;
    }

    public Date getTime() {
        return time;
    }
    public void setTime(Date time) {
        this.time = time;
    }

    public ArrayList<String> getMedia_images() {
        return media_images;
    }
    public void setMedia_images(ArrayList<String> media_images) {
        this.media_images = media_images;
    }

    public void setProf_image(String prof_image) {
        this.prof_image = prof_image;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getProf_image() {
        return prof_image;
    }

    public void setUser(String user) {
        this.userName = user;
    }
    public String getUser() {
        return userName;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public String getContent() {
        return content;
    }
    public String getReTweetedByName() {
        return reTweetedByName;
    }

    public void setReTweetedByName(String reTweetedByName) {
        this.reTweetedByName = reTweetedByName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Tweet> CREATOR = new Creator<Tweet>() {
        @Override
        public Tweet createFromParcel(Parcel in) {
            return new Tweet(in);
        }

        @Override
        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };
}
