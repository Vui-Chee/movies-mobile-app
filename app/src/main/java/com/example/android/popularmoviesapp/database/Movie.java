package com.example.android.popularmoviesapp.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "movie")
public class Movie implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int databaseId;
    private int movieId;
    private String title;
    private String releaseDate;
    private String posterPath;
    private Double voteAverage;
    private String plotSynopsis; // overview section


    @Ignore
    public Movie(int movieId, String title, String releaseDate, String posterPath, Double voteAverage, String plotSynopsis) {
        this.movieId = movieId;
        this.title = title;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
    }

    public Movie(int databaseId, int movieId, String title, String releaseDate, String posterPath, Double voteAverage, String plotSynopsis) {
        this.databaseId = databaseId;
        this.movieId = movieId;
        this.title = title;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public int getMovieId() {
        return movieId;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.movieId);
        dest.writeString(this.title);
        dest.writeString(this.releaseDate);
        dest.writeString(this.posterPath);
        dest.writeDouble(this.voteAverage);
        dest.writeString(this.plotSynopsis);
    }

    protected Movie(Parcel in) {
        this.movieId = in.readInt();
        this.title = in.readString();
        this.releaseDate = in.readString();
        this.posterPath = in.readString();
        this.voteAverage = in.readDouble();
        this.plotSynopsis = in.readString();
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
