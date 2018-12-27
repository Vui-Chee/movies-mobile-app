package com.example.android.popularmoviesapp.movie_details;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmoviesapp.R;
import com.example.android.popularmoviesapp.database.MovieDatabase;
import com.example.android.popularmoviesapp.models.Review;
import com.example.android.popularmoviesapp.reviews.ReviewListAdapter;
import com.example.android.popularmoviesapp.trailers.VideoListAdapter;
import com.example.android.popularmoviesapp.database.Movie;
import com.example.android.popularmoviesapp.models.MovieVideo;
import com.example.android.popularmoviesapp.utilities.AppExecutors;
import com.example.android.popularmoviesapp.utilities.MoviesDBJsonUtils;
import com.example.android.popularmoviesapp.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;

public class MovieDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<String>,
        VideoListAdapter.VideoListAdapterOnBindImage,
        VideoListAdapter.VideoListAdapterOnClickHandler {

    public static final String MOVIE_KEY = "curr_movie";
    private final int FETCH_VIDEOS_LOADER_ID = 2987;
    private final int FETCH_REVIEWS_LOADER_ID = 1852;
    private final String SAVE_MOVIE_VIDEOS_KEY = "save_movie_videos_key";
    private final String SAVE_MOVIE_REVIEWS_KEY = "save_movie_reviews_key";

    private ImageView moviePosterView;
    private TextView movieTitleTextView;
    private TextView movieRatingTextView;
    private TextView movieDateTextView;
    private TextView moviePlotTextView;

    private RecyclerView movieTrailersView;
    private VideoListAdapter movieVideosListAdapter;

    private RecyclerView reviewsView;
    private ReviewListAdapter reviewListAdapter;

    private String movieVideosString;
    private String movieReviewsString;

    private Button favouriteButton;
    private boolean isFavourite = false;
    private MovieDatabase mDb;
    private Movie currMovie;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Log.d("DETAILS : ", " on create");

        // Store context for later use.
        mContext = this;
        // Initialize DB instance.
        mDb = MovieDatabase.getInstance(this);

        prepareViewReferences();
        setUpButtonListeners();
        String movies_details_title = "Movie Details";
        setTitle(movies_details_title);

        ActionBar actionBar = this.getSupportActionBar();
        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra(MOVIE_KEY)) {
            currMovie = intentThatStartedThisActivity.getParcelableExtra(MOVIE_KEY);
            setUpMovieDetailsView(currMovie);

            MovieDetailsViewModelFactory factory = new MovieDetailsViewModelFactory(mDb, currMovie.getMovieId());
            final MovieDetailsViewModel viewModel = ViewModelProviders.of(this, factory).get(MovieDetailsViewModel.class);
            viewModel.getMovie().observe(this, new Observer<Movie>() {
                @Override
                public void onChanged(@Nullable Movie movie) {

                    viewModel.getMovie().removeObserver(this);

                    if (null == movie) return;

                    Log.d("DETAILS", "updating favourite button in ViewModel");

                    isFavourite = true;

                    if (movie.getMovieId() == currMovie.getMovieId()) {
                        toggleButtonUi(mContext, true);
                    }
                }
            });
        }

        if (null != savedInstanceState) {
            if (savedInstanceState.containsKey(SAVE_MOVIE_VIDEOS_KEY)) {
                movieVideosString = savedInstanceState.getString(SAVE_MOVIE_VIDEOS_KEY);
            }

            if (savedInstanceState.containsKey(SAVE_MOVIE_REVIEWS_KEY)) {
                movieReviewsString = savedInstanceState.getString(SAVE_MOVIE_REVIEWS_KEY);
            }
        }

        prepareVideosLoader();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the MoviesActivity.
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpButtonListeners() {

        View.OnClickListener favouriteMovieListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (null == currMovie) return;
                isFavourite = !isFavourite;
                updateDbAndUI(isFavourite);
            }
        };

        favouriteButton.setOnClickListener(favouriteMovieListener);
    }

    private void updateDbAndUI(final boolean isInsert) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                if (isInsert) {
                    // This movie does not exist in database, so insert it.
                    mDb.movieDao().insertMovie(currMovie);
                } else {
                    mDb.movieDao().deleteByMovieId(currMovie.getMovieId());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toggleButtonUi(mContext, isInsert);
                    }
                });
            }
        });
    }

    private void toggleButtonUi(Context context, boolean isInsert) {
        int color = ContextCompat.getColor(context, R.color.light_gray);
        if (isInsert) {
            color = ContextCompat.getColor(context, R.color.special_yellow);
            favouriteButton.setTextColor(Color.BLACK);
            favouriteButton.setText(R.string.already_favourite);
        } else {
            favouriteButton.setTextColor(Color.WHITE);
            favouriteButton.setText(R.string.mark_as_favourite);
        }
        favouriteButton.setBackgroundColor(color);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_MOVIE_VIDEOS_KEY, movieVideosString);
        outState.putString(SAVE_MOVIE_REVIEWS_KEY, movieReviewsString);
    }

    private void prepareVideosLoader() {
        LoaderManager loaderManager = getSupportLoaderManager();
        initLoader(FETCH_VIDEOS_LOADER_ID, loaderManager);
        initLoader(FETCH_REVIEWS_LOADER_ID, loaderManager);
    }

    private void initLoader(int loaderId, LoaderManager loaderManager) {
        Loader<String> loader = loaderManager.getLoader(loaderId);
        if (null == loader) {
            loaderManager.initLoader(loaderId, null, this);
        } else {
            loaderManager.restartLoader(loaderId, null, this);
        }
    }

    private void prepareViewReferences() {
        moviePosterView = findViewById(R.id.iv_movie_details_poster);
        movieTitleTextView = findViewById(R.id.tv_movie_details_title);
        movieRatingTextView = findViewById(R.id.tv_rating);
        movieDateTextView = findViewById(R.id.tv_release_date);
        moviePlotTextView = findViewById(R.id.tv_plot);

        // For displaying movie trailers.
        movieTrailersView = findViewById(R.id.rv_videos);
        movieTrailersView.setLayoutManager(new LinearLayoutManager(this));
        movieVideosListAdapter = new VideoListAdapter(this);
        movieTrailersView.setAdapter(movieVideosListAdapter);

        // For displaying reviews.
        reviewsView = findViewById(R.id.rv_reviews);
        reviewsView.setLayoutManager(new LinearLayoutManager(this));
        reviewListAdapter = new ReviewListAdapter();
        reviewsView.setAdapter(reviewListAdapter);

        favouriteButton = findViewById(R.id.button_favourite_movie);
    }

    private void setUpMovieDetailsView(Movie currMovie) {

        if (currMovie == null) return;

        movieTitleTextView.setText(currMovie.getTitle());
        movieRatingTextView.setText(String.valueOf(currMovie.getVoteAverage()));
        movieDateTextView.setText(currMovie.getReleaseDate());
        moviePlotTextView.setText(currMovie.getPlotSynopsis());
        Uri posterUri = NetworkUtils.buildPosterUri(currMovie.getPosterPath());
        Picasso.with(this).load(posterUri).into(moviePosterView);
    }

    @Override
    public Loader<String> onCreateLoader(final int id, Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if (null != movieVideosString && FETCH_VIDEOS_LOADER_ID == id) {
                    deliverResult(movieVideosString);
                } else if (null != movieReviewsString && FETCH_REVIEWS_LOADER_ID == id) {
                    deliverResult(movieReviewsString);
                } else {
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {

                if (null == currMovie) {
                    return null;
                }

                String responseDataStr = null;
                URL url = null;
                // With loader id, you decide which query to execute.
                try {

                    if (FETCH_VIDEOS_LOADER_ID == id) {
                        url = NetworkUtils.buildMovieVideosUrl(currMovie.getMovieId());
                    } else if (FETCH_REVIEWS_LOADER_ID == id) {
                        url = NetworkUtils.buildReviewUrl(currMovie.getMovieId(), 1);
                    }
                    responseDataStr = NetworkUtils.getResponseFromHttp(url);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return responseDataStr;
            }

            @Override
            public void deliverResult(String data) {
                if (FETCH_VIDEOS_LOADER_ID == id) {
                    movieVideosString = data;
                } else if (FETCH_REVIEWS_LOADER_ID == id) {
                    movieReviewsString = data;
                }
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String dataStr) {

        try {

            if (FETCH_VIDEOS_LOADER_ID == loader.getId()) {

                MovieVideo[] videos = MoviesDBJsonUtils.getVideosFromJson(dataStr);
                if (null != videos && videos.length > 0) {
                    movieVideosListAdapter.setVideosData(videos);
                } else {
                    Log.d("DEBUG : ", "No videos fetched");
                }

            } else if (FETCH_REVIEWS_LOADER_ID == loader.getId()) {

                Review[] reviews = MoviesDBJsonUtils.getReviewsFromJson(dataStr);
                if (null != reviews && reviews.length > 0) {
                    reviewListAdapter.setReviewsData(reviews);
                } else {
                    Log.d("DEBUG : ", "No reviews fetched");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // Do nothing.
    }

    @Override
    public void loadImageIntoView(VideoListAdapter.VideoViewHolder holder, String key) {
        NetworkUtils.loadThumbnailPicasso(this, holder, key);
    }

    @Override
    public void onClick(MovieVideo video) {
        String videoKey = video.getKey();

        Uri webpage = NetworkUtils.buildYoutubeVideoUri(videoKey);

        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

        if (null != intent.resolveActivity(getPackageManager())) {
            startActivity(intent);
        }
    }
}
