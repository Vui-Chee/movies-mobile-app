package com.example.android.popularmoviesapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.popularmoviesapp.database.Movie;

public class MovieActivityState implements Parcelable {

    private String topic;
    private int pageNum;
    private int totalPages;
    private Movie[] movies;

    public MovieActivityState() {}

    protected MovieActivityState(Parcel in) {
        this.topic = in.readString();
        this.pageNum = in.readInt();
        this.totalPages = in.readInt();
        this.movies = (Movie[]) in.readParcelableArray(Movie.class.getClassLoader());
    }

    public String getTopic() {
        return topic;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public Movie[] getMovies() {
        return movies;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setMovies(Movie[] movies) {
        this.movies = movies;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.topic);
        dest.writeInt(this.pageNum);
        dest.writeInt(this.totalPages);
        if (null != movies) {
            dest.writeParcelableArray(movies, movies.length);
        } else {
            dest.writeParcelableArray(new Movie[0], 0);
        }
    }

    public static final Parcelable.Creator<MovieActivityState> CREATOR = new Parcelable.Creator<MovieActivityState>() {
        @Override
        public MovieActivityState createFromParcel(Parcel source) {
            return new MovieActivityState(source);
        }

        @Override
        public MovieActivityState[] newArray(int size) {
            return new MovieActivityState[size];
        }
    };
}
