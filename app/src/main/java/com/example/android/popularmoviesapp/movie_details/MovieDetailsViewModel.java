package com.example.android.popularmoviesapp.movie_details;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.example.android.popularmoviesapp.database.Movie;
import com.example.android.popularmoviesapp.database.MovieDatabase;

public class MovieDetailsViewModel extends ViewModel {

    private static final String TAG = MovieDetailsViewModel.class.getSimpleName();

    private LiveData<Movie> movie;

    public MovieDetailsViewModel(MovieDatabase mDb, int movieId) {
        Log.d(TAG, "Query favourite movie from database.");
        movie = mDb.movieDao().loadMovieByMovieId(movieId);
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }
}
