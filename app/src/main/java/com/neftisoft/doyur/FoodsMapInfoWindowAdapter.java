package com.neftisoft.doyur;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.StringTokenizer;

class FoodsMapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;

    FoodsMapInfoWindowAdapter(Context context) {
        mWindow = LayoutInflater.from(context).inflate(R.layout.item_food_map_info, null);
    }

    private void setElementsToItem(final Marker marker, View view) {
        Handler mHandler = new Handler();

        String name = marker.getTitle();
        String snippet = marker.getSnippet();

        StringTokenizer tokens = new StringTokenizer(snippet, ",");
        String price = tokens.nextToken();
        final String photoUrl = tokens.nextToken();
        String ownerUid = tokens.nextToken();

        TextView foodNameMapTextView = view.findViewById(R.id.foodMapNameTextView);
        TextView foodPriceMapTextView = view.findViewById(R.id.foodMapPriceTextView);
        TextView foodUidMapTextView = view.findViewById(R.id.foodMapUidTextView);
        final ImageView foodPhotoMapImageView = view.findViewById(R.id.foodMapImageView);

        mWindow.findViewById(R.id.foodMapImageBackground).setVisibility(View.VISIBLE);
        mWindow.findViewById(R.id.foodMapTextBackground).setVisibility(View.VISIBLE);

        foodNameMapTextView.setText(name);
        foodPriceMapTextView.setText(price);
        foodUidMapTextView.setText(ownerUid);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Picasso.get()
                        .load(photoUrl)
                        .fit()
                        .centerCrop()
                        .into(foodPhotoMapImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                if (marker.isInfoWindowShown()) {
                                    marker.hideInfoWindow();
                                    marker.showInfoWindow();
                                } else {
                                    mWindow.findViewById(R.id.foodMapImageBackground).setVisibility(View.GONE);
                                    mWindow.findViewById(R.id.foodMapTextBackground).setVisibility(View.GONE);
                                }
                            }
                            @Override
                            public void onError(Exception e) {
                                /*
                                Toast.makeText(foodPhotoMapImageView.getContext(), "Yüklenirken bir sorun oluştu! Yemek kaldırılmış olabilir.",
                                        Toast.LENGTH_LONG).show();
                                 */
                            }
                        });
            }
        },1200);

        Picasso.get()
                .load(photoUrl)
                .fit()
                .centerCrop()
                .into(foodPhotoMapImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (marker.isInfoWindowShown()) {
                            marker.hideInfoWindow();
                            marker.showInfoWindow();
                        } else {
                            mWindow.findViewById(R.id.foodMapImageBackground).setVisibility(View.GONE);
                            mWindow.findViewById(R.id.foodMapTextBackground).setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        /*
                        Toast.makeText(foodPhotoMapImageView.getContext(), "Yüklenirken bir sorun oluştu! Yemek kaldırılmış olabilir.",
                                Toast.LENGTH_LONG).show();
                         */
                    }
                });
    }

    @Override
    public View getInfoWindow(Marker marker) {
        setElementsToItem(marker, mWindow);
        return mWindow;
    }
    @Override
    public View getInfoContents(Marker marker) {
        //setElementsToItem(marker, mWindow);
        return null;
    }
}
