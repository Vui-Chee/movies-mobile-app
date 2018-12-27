package com.example.android.popularmoviesapp.models;

public class MovieVideo {

    private int movieId;
    private String videoId;
    private String name;
    private String key;
    private String type;

    public MovieVideo(int movieId, String videoId, String name, String key, String type) {
        this.movieId = movieId;
        this.videoId = videoId;
        this.name = name;
        this.key = key;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }
}
