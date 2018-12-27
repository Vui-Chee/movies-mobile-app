package com.example.android.popularmoviesapp.movie_grid;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmoviesapp.R;
import com.example.android.popularmoviesapp.database.Movie;

public class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {

    private Movie[] mMovies;
    private final Context mContext;

    public MovieGridAdapter(Context context) {
        mContext = context;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie currMovie = mMovies[position];
        String posterPath = currMovie.getPosterPath();
        holder.noPosterTextView.setVisibility(View.INVISIBLE);
        String POSTER_PATH_NULL = "null";
        if (posterPath.equals(POSTER_PATH_NULL)) holder.noPosterTextView.setVisibility(View.VISIBLE);
        ((MovieGridAdapterOnBindImage) mContext).loadImageIntoView(holder, posterPath);
    }

    @Override
    public int getItemViewType(int position) {
        // IMPT : Prevents inconsistent update of multiple views inside viewholder during scrolling.
        return position;
    }

    @Override
    public int getItemCount() {
        return mMovies == null ? 0 : mMovies.length;
    }

    public void setMoviesData(Movie[] movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView moviePosterImageView;
        private final TextView noPosterTextView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            moviePosterImageView = itemView.findViewById(R.id.iv_movie_list_item);
            noPosterTextView = itemView.findViewById(R.id.tv_no_poster_message);
            itemView.setOnClickListener(this);
        }

        public ImageView getMoviePosterImageView() {
            return moviePosterImageView;
        }

        public TextView getNoPosterTextView() {
            return noPosterTextView;
        }

        @Override
        public void onClick(View view) {
            ((MovieGridAdapterOnClickHandler) mContext).onClick(mMovies[getAdapterPosition()]);
        }
    }

    public interface MovieGridAdapterOnBindImage {
        void loadImageIntoView(MovieViewHolder holder, String posterPath);
    }

    public interface MovieGridAdapterOnClickHandler {
        void onClick(Movie movie);
    }
}
