package com.simleman.moritwitter;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by hayatomoritani on 10/28/16.
 */

public class Tweet {
    String user;
    String content;
    private String prof_image;
    private ArrayList<String> media_images;

    public ArrayList<String> getMedia_images() {
        return media_images;
    }

    public void setMedia_images(ArrayList<String> media_images) {
        this.media_images = media_images;
    }

    public String getProf_image() {
        return prof_image;
    }

    public void setProf_image(String prof_image) {
        this.prof_image = prof_image;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }
}
