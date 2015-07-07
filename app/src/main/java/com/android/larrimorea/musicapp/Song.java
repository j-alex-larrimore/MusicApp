package com.android.larrimorea.musicapp;

import android.net.Uri;

/**
 * Created by Alex on 7/7/2015.
 */
public class Song {
    private String mUrl;
    private String mTitle;
    private String mArtist;


    public Song(String url, String title, String artist){
        mUrl = url;
        mTitle = title;
        mArtist = artist;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getArtist() {
        return mArtist;
    }
}
