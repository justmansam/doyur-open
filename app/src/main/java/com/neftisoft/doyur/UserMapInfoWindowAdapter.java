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

class UserMapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;

    UserMapInfoWindowAdapter(Context context) {
        mWindow = LayoutInflater.from(context).inflate(R.layout.item_user_map_info, null);
    }

    private void setElementsToItem(final Marker marker, View view) {
        Handler mHandler = new Handler();

        final String userAvatar = marker.getTitle();
        String userUid = marker.getSnippet();

        TextView userUidMapTextView = view.findViewById(R.id.userMapUidTextView);
        final ImageView userPhotoMapImageView = view.findViewById(R.id.userMapImageView);

        mWindow.findViewById(R.id.userMapImageBackground).setVisibility(View.VISIBLE);

        userUidMapTextView.setText(userUid);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Picasso.get()
                        .load(userAvatar)
                        .fit()
                        .centerCrop()
                        .into(userPhotoMapImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                if (marker.isInfoWindowShown()) {
                                    marker.hideInfoWindow();
                                    marker.showInfoWindow();
                                } else {
                                    mWindow.findViewById(R.id.userMapImageBackground).setVisibility(View.GONE);
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
                .load(userAvatar)
                .fit()
                .centerCrop()
                .into(userPhotoMapImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (marker.isInfoWindowShown()) {
                            marker.hideInfoWindow();
                            marker.showInfoWindow();
                        } else {
                            mWindow.findViewById(R.id.userMapImageBackground).setVisibility(View.GONE);
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
