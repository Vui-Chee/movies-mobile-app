package com.example.android.popularmoviesapp.movie_grid;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.example.android.popularmoviesapp.database.Movie;
import com.example.android.popularmoviesapp.database.MovieDatabase;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<Movie>> movies;

    public MainViewModel(Application application) {
        super(application);
        MovieDatabase mDb = MovieDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving tasks from the database.");
        movies = mDb.movieDao().loadAllMovies();
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }
}
