package com.example.android.popularmoviesapp.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movie")
    LiveData<List<Movie>> loadAllMovies();

    @Query("SELECT * FROM movie WHERE movieId = :movieId")
    LiveData<Movie> loadMovieByMovieId(int movieId);

    @Insert
    void insertMovie(Movie movie);

    @Query("DELETE FROM movie")
    void clearAllMovies();

    @Query("DELETE FROM movie WHERE movieId = :movieId")
    void deleteByMovieId(int movieId);

    @Query("SELECT DISTINCT(COUNT(*)) FROM movie")
    int numMoviesInDb();
}
