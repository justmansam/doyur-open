package com.neftisoft.doyur;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

// IT IS FOR REVIEW ACTIVITY !!!

public class ReviewAdapter extends ArrayAdapter<FoodReview> {
    public ReviewAdapter(Context context, int resource, List<FoodReview> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_review, parent, false);
        }

        ImageView reviewerAvatarImageView = convertView.findViewById(R.id.review_avatar_imageview);
        TextView reviewerNameTextView = convertView.findViewById(R.id.review_reviewer_textview);
        TextView reviewerRateTextView = convertView.findViewById(R.id.review_rating_textview);
        TextView reviewTextView = convertView.findViewById(R.id.review_textview);
        TextView reviewDateTextView = convertView.findViewById(R.id.review_date_textview);
        TextView reviewerUidTextView = convertView.findViewById(R.id.review_uid_textview);
        TextView reviewKeyTextView = convertView.findViewById(R.id.review_key_textview);
        
        FoodReview rateReview = getItem(position);

        boolean isPhoto = false;
        if (rateReview != null) {

            isPhoto = rateReview.getReviewerAvatar() != null;

            if (isPhoto) {
                Glide.with(reviewerAvatarImageView.getContext())
                        .load(rateReview.getReviewerAvatar())
                        .centerCrop()
                        .into(reviewerAvatarImageView);
            }

            reviewerNameTextView.setText(rateReview.getReviewerName());

            if (rateReview.getReviewerRate() != null) {
                if (rateReview.getReviewerRate().equals("10")) {
                    reviewerRateTextView.setTextColor(Color.parseColor("#1B5E20"));
                }
                else if (rateReview.getReviewerRate().equals("9")) {
                    reviewerRateTextView.setTextColor(Color.parseColor("#66BB6A"));
                }
                else if (rateReview.getReviewerRate().equals("8")) {
                    reviewerRateTextView.setTextColor(Color.parseColor("#D4E157"));
                }
                else if (rateReview.getReviewerRate().equals("7")) {
                    reviewerRateTextView.setTextColor(Color.parseColor("#FFEE58"));
                }
                else if (rateReview.getReviewerRate().equals("6")) {
                    reviewerRateTextView.setTextColor(Color.parseColor("#FFA726"));
                }
                else if (rateReview.getReviewerRate().equals("5")) {
                    reviewerRateTextView.setTextColor(Color.parseColor("#FF7043"));
                }
                else if (rateReview.getReviewerRate().equals("4") || rateReview.getReviewerRate().equals("3") || rateReview.getReviewerRate().equals("2") || rateReview.getReviewerRate().equals("1")) {
                    reviewerRateTextView.setTextColor(Color.parseColor("#BF360C"));
                }
            }

            reviewerRateTextView.setText(rateReview.getReviewerRate());

            reviewTextView.setText(rateReview.getReview());

            reviewDateTextView.setText(rateReview.getDateNow());

            reviewerUidTextView.setText(rateReview.getReviewerUid());

            reviewKeyTextView.setText(rateReview.getReviewKey());

        }

        return convertView;
    }
}
