package com.neftisoft.doyur;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.StringTokenizer;

class StoreMapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mStoreWindow;

    StoreMapInfoWindowAdapter(Context context) {
        mStoreWindow = LayoutInflater.from(context).inflate(R.layout.item_store_map_info, null);
    }

    private void setElementsToItem(final Marker storeMarker, View view) {
        Handler mHandler = new Handler();

        String storeName = storeMarker.getTitle();
        String snippet = storeMarker.getSnippet();

        StringTokenizer tokens = new StringTokenizer(snippet, "#");
        
        String storeUid = tokens.nextToken();
        final String storeMainPhotoUrl = tokens.nextToken();
        String storeInfo = tokens.nextToken();

        TextView storeNameMapTextView = view.findViewById(R.id.mapStoreNameTextView);
        TextView storeInfoMapTextView = view.findViewById(R.id.mapStoreInfoTextView);
        TextView storeUidMapTextView = view.findViewById(R.id.mapStoreUidTextView);
        final ImageView storeMainPhotoMapImageView = view.findViewById(R.id.mapStoreMainImageView);

        mStoreWindow.findViewById(R.id.storeMapImageBackground).setVisibility(View.VISIBLE);
        mStoreWindow.findViewById(R.id.storeMapTextBackground).setVisibility(View.VISIBLE);

        storeNameMapTextView.setText(storeName);
        storeInfoMapTextView.setText(storeInfo);
        storeUidMapTextView.setText(storeUid);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Picasso.get()
                        .load(storeMainPhotoUrl)
                        .fit()
                        .centerCrop()
                        .into(storeMainPhotoMapImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                if (storeMarker.isInfoWindowShown()) {
                                    storeMarker.hideInfoWindow();
                                    storeMarker.showInfoWindow();
                                } else {
                                    mStoreWindow.findViewById(R.id.storeMapImageBackground).setVisibility(View.GONE);
                                    mStoreWindow.findViewById(R.id.storeMapTextBackground).setVisibility(View.GONE);
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
                .load(storeMainPhotoUrl)
                .fit()
                .centerCrop()
                .into(storeMainPhotoMapImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (storeMarker.isInfoWindowShown()) {
                            storeMarker.hideInfoWindow();
                            storeMarker.showInfoWindow();
                        } else {
                            mStoreWindow.findViewById(R.id.storeMapImageBackground).setVisibility(View.GONE);
                            mStoreWindow.findViewById(R.id.storeMapTextBackground).setVisibility(View.GONE);
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
    public View getInfoWindow(Marker storeMarker) {
        setElementsToItem(storeMarker, mStoreWindow);
        return mStoreWindow;
    }
    @Override
    public View getInfoContents(Marker storeMarker) {
        //setElementsToItem(marker, mWindow);
        return null;
    }
}
