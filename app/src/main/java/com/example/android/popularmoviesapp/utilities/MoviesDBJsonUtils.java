package com.example.android.popularmoviesapp.utilities;

import com.example.android.popularmoviesapp.database.Movie;
import com.example.android.popularmoviesapp.models.MovieVideo;
import com.example.android.popularmoviesapp.models.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class MoviesDBJsonUtils {

    private static final String ERRORS = "errors";
    private static final String SUCCESS = "success";

    private static final String RESULTS_PARAM = "results";
    private static final String ID_PARAM = "id";

    // For popular/top-rated movies search.
    private static final String TITLE_PARAM = "title";
    private static final String RELEASE_DATE_PARAM = "release_date";
    private static final String POSTER_PATH_PARAM = "poster_path";
    private static final String VOTE_AVERAGE_PARAM = "vote_average";
    private static final String PLOT_PARAM = "overview";
    private static final String TOTAL_PAGES_PARAM = "total_pages";

    // For videos related to movie.
    private static final String VIDEO_NAME_PARAM = "name";
    private static final String VIDEO_TYPE_PARAM = "type";
    private static final String VIDEO_KEY_PARAM = "key";

    // For reviews related to movie.
    private static final String AUTHOR_NAME_PARAM = "author";
    private static final String REVIEW_CONTENT_PARAM = "content";
    private static final String REVIEW_URL_PARAM = "url";

    private static boolean validateJson(JSONObject json) throws JSONException {
        // Something has gone wrong in the api request.
        if (json.has(ERRORS)) {
            return true;
        }

        // Request returned "success" : false.
        if (json.has(SUCCESS) && !json.getBoolean(SUCCESS)) {
            return true;
        }

        return false;
    }

    public static int getTotalPagesFromJson(String moviesJsonStr) throws JSONException {

        if (moviesJsonStr == null || moviesJsonStr.length() == 0) {
            return 0;
        }

        // Passing an empty string causes an error.
        JSONObject movieListJson = new JSONObject(moviesJsonStr);

        if (validateJson(movieListJson)) {
            return 0;
        }

        return movieListJson.getInt(TOTAL_PAGES_PARAM);
    }

    // IMPT : The default number of results/page is 20.
    public static Movie[] getMoviesFromJson(String moviesJsonStr) throws JSONException {

        if (moviesJsonStr == null || moviesJsonStr.length() == 0) {
            return null;
        }

        JSONObject movieListJson = new JSONObject(moviesJsonStr);

        if (validateJson(movieListJson)) {
            return null;
        }

        JSONArray movieResults = movieListJson.getJSONArray(RESULTS_PARAM);

        Movie[] parsedMovieData = new Movie[movieResults.length()];

        for (int i = 0; i < movieResults.length(); i++) {
            JSONObject movieJson = movieResults.getJSONObject(i);
            int movieId = movieJson.getInt(ID_PARAM);
            String title = movieJson.getString(TITLE_PARAM);
            String releaseDate = movieJson.getString(RELEASE_DATE_PARAM);
            String posterPath = movieJson.getString(POSTER_PATH_PARAM);
            double voteAverage = movieJson.getDouble(VOTE_AVERAGE_PARAM);
            String plotSynopsis = movieJson.getString(PLOT_PARAM);
            parsedMovieData[i] = new Movie(movieId, title, releaseDate, posterPath, voteAverage, plotSynopsis);
        }

        return parsedMovieData;
    }

    public static MovieVideo[] getVideosFromJson(String videosJsonString) throws JSONException {

        if (null == videosJsonString || videosJsonString.length() == 0) {
            return null;
        }

        JSONObject videosJson = new JSONObject(videosJsonString);
        JSONArray videoResults = videosJson.getJSONArray(RESULTS_PARAM);
        MovieVideo[] parsedVideoData = new MovieVideo[videoResults.length()];

        int movieId = videosJson.getInt(ID_PARAM);

        for (int i = 0; i < videoResults.length(); i++) {
            JSONObject singleVideoJson = videoResults.getJSONObject(i);
            String videoId = singleVideoJson.getString(ID_PARAM);
            String videoName = singleVideoJson.getString(VIDEO_NAME_PARAM);
            String videoType = singleVideoJson.getString(VIDEO_TYPE_PARAM);
            String videoKey = singleVideoJson.getString(VIDEO_KEY_PARAM);
            parsedVideoData[i] = new MovieVideo(movieId, videoId, videoName, videoKey, videoType);
        }

        return parsedVideoData;
    }

    public static Review[] getReviewsFromJson(String reviewsJsonString) throws JSONException {

        if (null == reviewsJsonString || reviewsJsonString.length() == 0) {
            return null;
        }

        JSONObject reviewsJson = new JSONObject(reviewsJsonString);
        JSONArray reviewResults = reviewsJson.getJSONArray(RESULTS_PARAM);
        Review[] reviews = new Review[reviewResults.length()];

        int movieId = reviewsJson.getInt(ID_PARAM);

        for (int i = 0; i < reviewResults.length(); i++) {
            JSONObject singleReviewJson = reviewResults.getJSONObject(i);
            String authorName = singleReviewJson.getString(AUTHOR_NAME_PARAM);
            String reviewContent = singleReviewJson.getString(REVIEW_CONTENT_PARAM);
            String reviewId = singleReviewJson.getString(ID_PARAM);
            String reviewUrl = singleReviewJson.getString(REVIEW_URL_PARAM);
            reviews[i] = new Review(movieId, reviewId, authorName, reviewContent, reviewUrl);
        }

        return reviews;
    }
}
