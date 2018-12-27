package com.example.android.popularmoviesapp.models;

public class Review {

    private int movieId;
    private String reviewId;
    private String author;
    private String content;
    private String reviewUrl;

    public Review(int movieId, String reviewId, String author, String content, String reviewUrl) {
        this.movieId = movieId;
        this.reviewId = reviewId;
        this.author = author;
        this.content = content;
        this.reviewUrl = reviewUrl;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
