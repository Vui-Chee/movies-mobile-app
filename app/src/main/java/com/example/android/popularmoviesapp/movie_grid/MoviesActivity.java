package com.example.android.popularmoviesapp.movie_grid;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmoviesapp.movie_details.MovieDetailsActivity;
import com.example.android.popularmoviesapp.R;
import com.example.android.popularmoviesapp.database.Movie;
import com.example.android.popularmoviesapp.models.MovieActivityState;
import com.example.android.popularmoviesapp.utilities.MoviesDBJsonUtils;
import com.example.android.popularmoviesapp.utilities.NetworkUtils;

import com.example.android.popularmoviesapp.movie_grid.MovieGridAdapter.MovieViewHolder;
import com.example.android.popularmoviesapp.movie_grid.MovieGridAdapter.MovieGridAdapterOnClickHandler;
import com.example.android.popularmoviesapp.movie_grid.MovieGridAdapter.MovieGridAdapterOnBindImage;


import java.net.URL;
import java.util.List;

public class MoviesActivity extends AppCompatActivity implements
        MovieGridAdapterOnClickHandler, MovieGridAdapterOnBindImage, LoaderManager.LoaderCallbacks<Movie[]> {

    private final int FETCH_MOVIES_LOADER_ID = 1234;

    private final String MOVIE_TOPIC = "movie_topic";
    private final String MOVIES_PAGENUM = "movies_pagenum";
    private final String POPULAR = "popular";
    private final String TOP_RATED = "top_rated";

    private final String SAVE_MOVIE_ACTIVITY_STATE_KEY = "save_movie_activity_key";    
    private final String SAVE_MENU_TYPE_KEY = "save_menu_type_key";

    private final String POPULAR_MOVIES_TITLE = "Popular Movies";
    private final String TOP_RATED_MOVIES_TITLE = "Top Rated Movies";
    private final String FAVOURITE_MOVIES_TITLE = "Favourite Movies";

    private String currentTopic = POPULAR;
    private int pageNum = 1;
    private int totalPages = 0;
    private Movie[] moviesData;
    private boolean fetchNewPage = false;

    private RecyclerView moviesGridView;
    private ProgressBar pbLoadingIndicator;
    private Button reloadButton;
    private TextView noResultsTextView;
    private LinearLayout errorLayout;
    private Button prevButton;
    private Button nextButton;

    private MovieGridAdapter movieGridAdapter;

    private LoaderManager loaderManager = getSupportLoaderManager();

    private enum WhichMenu {
        POPULAR,
        TOP_RATED,
        FAVOURITES
    }

    private WhichMenu menuType = WhichMenu.POPULAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        // Log.d("DEBUG : ", " on create");

        prepareViewReferences();
        setUpButtonListeners();

        if (null != savedInstanceState) {

            if (savedInstanceState.containsKey(SAVE_MOVIE_ACTIVITY_STATE_KEY)) {
                MovieActivityState movieActivityState = savedInstanceState.getParcelable(SAVE_MOVIE_ACTIVITY_STATE_KEY);
                currentTopic = movieActivityState.getTopic();
                pageNum = movieActivityState.getPageNum();
                totalPages = movieActivityState.getTotalPages();
                moviesData = movieActivityState.getMovies();
            }

            if (savedInstanceState.containsKey(SAVE_MENU_TYPE_KEY)) {
                String menuTypeString = savedInstanceState.getString(SAVE_MENU_TYPE_KEY);
                menuType = WhichMenu.valueOf(menuTypeString);
            }
        }

        if (WhichMenu.FAVOURITES == menuType) {
            setupViewModel();
        } else {
            loadAndDisplayMovies();
        }

        setUpMenuTitle(menuType);
    }

    private void setUpMenuTitle(WhichMenu type) {
        if (WhichMenu.POPULAR == type) {
            setTitle(POPULAR_MOVIES_TITLE);
        } else if (WhichMenu.TOP_RATED == type) {
            setTitle(TOP_RATED_MOVIES_TITLE);
        } else if (WhichMenu.FAVOURITES == type) {
            setTitle(FAVOURITE_MOVIES_TITLE);
        }
    }

    private void togglePrevNextButtons(boolean toShow) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int default_height = 150;
        LinearLayout ll_layout = findViewById(R.id.ll_next_prev_buttons);
        int calcHeight = toShow ? default_height : 0;
        ll_layout.setLayoutParams(new LinearLayout.LayoutParams(width, calcHeight));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Log.d("DEBUG", "onSaveInstanceState");

        MovieActivityState currentActivityState = new MovieActivityState();
        currentActivityState.setTopic(currentTopic);
        currentActivityState.setPageNum(pageNum);
        currentActivityState.setTotalPages(totalPages);
        currentActivityState.setMovies(moviesData);
        outState.putParcelable(SAVE_MOVIE_ACTIVITY_STATE_KEY, currentActivityState);

        outState.putString(SAVE_MENU_TYPE_KEY, menuType.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        currentTopic = itemThatWasClickedId == R.id.action_popular ? POPULAR : TOP_RATED;
        pageNum = 1; // Reset pageNum to the first page.

        setMenuType(itemThatWasClickedId);
        setUpMenuTitle(menuType);

        if (itemThatWasClickedId == R.id.action_popular || itemThatWasClickedId == R.id.action_top_rated) {
            fetchNewPage = true;
            loadAndDisplayMovies();
            return true;
        }

        if (itemThatWasClickedId == R.id.action_favourites) {
            moviesGridView.scrollToPosition(0);
            setupViewModel();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setMenuType(int actionId) {
        if (R.id.action_popular == actionId) {
            menuType = WhichMenu.POPULAR;
        } else if (R.id.action_top_rated == actionId) {
            menuType = WhichMenu.TOP_RATED;
        } else if (R.id.action_favourites == actionId) {
            menuType = WhichMenu.FAVOURITES;
        }
    }

    private void setupViewModel() {

        togglePrevNextButtons(false);

        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        viewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {

                if (null == movies) return;

                if (WhichMenu.FAVOURITES == menuType) {
                    Log.d("DEBUG", "Updating movies from LiveData in ViewModel.");
                    Movie[] tempMovies = new Movie[movies.size()];
                    tempMovies = movies.toArray(tempMovies);
                    movieGridAdapter.setMoviesData(tempMovies);
                }
            }
        });
    }

    private void prepareViewReferences() {
        moviesGridView = findViewById(R.id.rv_gridview_movies);
        moviesGridView.setLayoutManager(new GridLayoutManager(this, 2));
        movieGridAdapter = new MovieGridAdapter(this);
        moviesGridView.setAdapter(movieGridAdapter);

        prevButton = findViewById(R.id.b_prev_page);
        nextButton = findViewById(R.id.b_next_page);
        pbLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        reloadButton = findViewById(R.id.reload_button);
        noResultsTextView = findViewById(R.id.tv_no_results_found);
        errorLayout = findViewById(R.id.ll_error_view);
    }

    @Override
    public void onClick(Movie movie) {
        Class destinationActivity = MovieDetailsActivity.class;
        Intent peekMovieDetailsIntent = new Intent(this, destinationActivity);
        peekMovieDetailsIntent.putExtra(MovieDetailsActivity.MOVIE_KEY, movie);
        this.startActivity(peekMovieDetailsIntent);
    }

    @Override
    public void loadImageIntoView(MovieViewHolder holder, String posterPath) {
        NetworkUtils.picassoLoadImageIntoView(this, posterPath, holder);
    }

    private void setUpButtonListeners() {

        final Context context = this;

        View.OnClickListener fetchResultsListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int prevPageNum = pageNum;
                int viewId = view.getId();

                if (viewId != R.id.reload_button) {
                    if (viewId == R.id.b_prev_page) {
                        pageNum = pageNum >= 2 ? pageNum - 1 : pageNum;

                        if (1 == pageNum) {
                            CharSequence firstPageText = "This is the first page.";
                            Toast
                                    .makeText(context, firstPageText, Toast.LENGTH_SHORT)
                                    .show();
                        }

                    } else if (viewId == R.id.b_next_page) {
                        pageNum = pageNum <= totalPages ? pageNum + 1 : pageNum;
                    }

                    // Only fetch a new page if the page number actually changes.
                    if (prevPageNum != pageNum) {
                        fetchNewPage = true;
                        loadAndDisplayMovies();
                    }
                } else {
                    loadAndDisplayMovies();
                }
            }
        };

        prevButton.setOnClickListener(fetchResultsListener);
        nextButton.setOnClickListener(fetchResultsListener);
        reloadButton.setOnClickListener(fetchResultsListener);
    }

    private Bundle fetchMoviesByTopicAndPageBundle(String topic, int pageNum) {

        if (null == topic || topic.length() == 0 || pageNum <= 0) return null;

        Bundle queryBundle = new Bundle();
        queryBundle.putString(MOVIE_TOPIC, currentTopic);
        queryBundle.putInt(MOVIES_PAGENUM, pageNum);
        return queryBundle;
    }

    private void loadAndDisplayMovies() {
        moviesGridView.scrollToPosition(0);
        togglePrevNextButtons(true);

        Bundle queryBundle = fetchMoviesByTopicAndPageBundle(currentTopic, pageNum);
        Loader<Movie[]> fetchMoviesLoader = loaderManager.getLoader(FETCH_MOVIES_LOADER_ID);
        if (null != fetchMoviesLoader) {
            loaderManager.restartLoader(FETCH_MOVIES_LOADER_ID, queryBundle, this);
        } else {
            loaderManager.initLoader(FETCH_MOVIES_LOADER_ID, queryBundle, this);
        }
    }

    private void showMoviesGrid() {
        moviesGridView.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.INVISIBLE);
        noResultsTextView.setVisibility(View.INVISIBLE);
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    private void showErrorView() {
        errorLayout.setVisibility(View.VISIBLE);
        moviesGridView.setVisibility(View.INVISIBLE);
        noResultsTextView.setVisibility(View.INVISIBLE);
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    private void hideMoviesGridOnLoading() {
        pbLoadingIndicator.setVisibility(View.VISIBLE);
        moviesGridView.setVisibility(View.INVISIBLE);
        errorLayout.setVisibility(View.INVISIBLE);
        noResultsTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<Movie[]> onCreateLoader(int id, final Bundle params) {
        return new AsyncTaskLoader<Movie[]>(this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if (null == params) return;

                if (fetchNewPage) {
                    fetchNewPage = false;
                    hideMoviesGridOnLoading();
                    forceLoad();
                    return;
                }

                if (null != moviesData) {
                    deliverResult(moviesData);
                } else {
                    hideMoviesGridOnLoading();
                    forceLoad();
                }
            }

            @Override
            public Movie[] loadInBackground() {

                String topic = params.getString(MOVIE_TOPIC);
                int pageNum = params.getInt(MOVIES_PAGENUM);

                URL moviesRequestUrl = NetworkUtils.buildMoviesUrl(topic, pageNum);

                try {
                    String moviesJsonStr = NetworkUtils.getResponseFromHttp(moviesRequestUrl);
                    Movie[] movies = MoviesDBJsonUtils.getMoviesFromJson(moviesJsonStr);
                    totalPages = MoviesDBJsonUtils.getTotalPagesFromJson(moviesJsonStr);
                    return movies;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            public void deliverResult(Movie[] movies) {
                moviesData = movies; // Cache the data.
                super.deliverResult(movies);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Movie[]> loader, Movie[] movies) {

        if (movies == null) {
            showErrorView();
        } else {
            if (movies.length == 0) {
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
                noResultsTextView.setVisibility(View.VISIBLE);
            } else {
                movieGridAdapter.setMoviesData(movies);
                showMoviesGrid();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Movie[]> loader) {
        // Currently not in used at the moment.
    }
}
