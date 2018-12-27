package com.example.android.popularmoviesapp.utilities;

import android.content.Context;
import android.net.Uri;

import com.example.android.popularmoviesapp.BuildConfig;
import com.example.android.popularmoviesapp.movie_grid.MovieGridAdapter.MovieViewHolder;
import com.example.android.popularmoviesapp.trailers.VideoListAdapter;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public final class NetworkUtils {

    private static final int CONNECTION_TIMEOUT = 2500;
    private static final int READ_TIMEOUT = 1500;

    private static final String MOVIEDB_BASEURL = "http://api.themoviedb.org/3/movie/";
    // Default poster size specified in URL.
    private static final String POSTER_BASEURL = "http://image.tmdb.org/t/p/w185/";

    private static final String API_KEY_PARAM  = "api_key";
    private static final String PAGENUM_PARAM  = "page";

    // TODO : Remove before submission !!!
    private static final String API_KEY = BuildConfig.API_KEY;

    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";
    private static final String VIDEOS_PATH = "videos";

    private static final String YOUTUBE_THUMBNAIL_BASE_URL = "https://img.youtube.com/vi/";
    private static final String YOUTUBE_DEFAULT_HIGH_QUALITY_IMG = "hqdefault.jpg";

    private static final String REVIEWS_PATH = "reviews";

    public static URL buildMoviesUrl(String topic, int pageNum) {
        Uri builtUri = Uri.parse(MOVIEDB_BASEURL).buildUpon()
                .appendPath(topic)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(PAGENUM_PARAM, String.valueOf(pageNum))
                .build();

        return buildUrlFromUri(builtUri);
    }

    public static URL buildMovieVideosUrl(int movieId) {

        Uri builtUri = Uri.parse(MOVIEDB_BASEURL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(VIDEOS_PATH)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();

        return buildUrlFromUri(builtUri);
    }

    public static URL buildReviewUrl(int movieId, int pageNum) {

        Uri builtUri = Uri.parse(MOVIEDB_BASEURL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(REVIEWS_PATH)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(PAGENUM_PARAM, String.valueOf(pageNum))
                .build();

        return buildUrlFromUri(builtUri);
    }

    public static URL buildUrlFromUri(Uri uri) {
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static Uri buildPosterUri(String posterPath) {
        return Uri.parse(NetworkUtils.POSTER_BASEURL).buildUpon()
                .appendEncodedPath(posterPath)
                .build();
    }

    public static Uri buildYoutubeVideoUri(String key) {
        return Uri.parse(YOUTUBE_BASE_URL + key).buildUpon().build();
    }

    public static void loadThumbnailPicasso(Context context, VideoListAdapter.VideoViewHolder holder, String key) {

        Uri uri = Uri.parse(YOUTUBE_THUMBNAIL_BASE_URL).buildUpon()
                .appendEncodedPath(key)
                .appendEncodedPath(YOUTUBE_DEFAULT_HIGH_QUALITY_IMG)
                .build();

        Picasso.with(context).load(uri).into(holder.getVideoThumbnail());
    }

    public static void picassoLoadImageIntoView(Context context, String posterPath, MovieViewHolder holder) {
        try {
            Uri posterUri = NetworkUtils.buildPosterUri(posterPath);
            Picasso.with(context).load(posterUri).into(holder.getMoviePosterImageView());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static String getResponseFromHttp(URL url) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
        urlConnection.setReadTimeout(READ_TIMEOUT);

        try {
            InputStream in = urlConnection.getInputStream();

            Scanner sc = new Scanner(in);
            sc.useDelimiter("\\A");

            boolean hasInput = sc.hasNext();
            if (hasInput) {
                return sc.next();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        finally {
            urlConnection.disconnect();
        }
    }
}
