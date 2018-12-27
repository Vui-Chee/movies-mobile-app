package com.example.android.popularmoviesapp.reviews;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmoviesapp.R;
import com.example.android.popularmoviesapp.models.Review;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewViewHolder> {

    private Review[] reviews;

    @Override
    public ReviewListAdapter.ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.review_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);

        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewListAdapter.ReviewViewHolder holder, int position) {
        Review review = reviews[position];
        holder.authorNameTextView.setText(review.getAuthor());
        holder.reviewContentTextView.setText(review.getContent());
    }

    @Override
    public int getItemViewType(int position) {
        // IMPT : Prevents inconsistent update of multiple views inside viewholder during scrolling.
        return position;
    }

    @Override
    public int getItemCount() {
        return null != reviews ? reviews.length : 0;
    }

    public void setReviewsData(Review[] reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        private TextView authorNameTextView;
        private TextView reviewContentTextView;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            authorNameTextView = itemView.findViewById(R.id.tv_review_author_name);
            reviewContentTextView = itemView.findViewById(R.id.tv_review_content);
        }
    }
}
