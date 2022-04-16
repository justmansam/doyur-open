package com.neftisoft.doyur;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;

import java.util.List;

// IT IS FOR STORE ACTIVITY (SAVING STORE) (ACCORDING TO THE LOCATION!!!)

public class StoreAdapter extends ArrayAdapter<FoodStore> {
    public StoreAdapter(Context context, int resource, List<FoodStore> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //TO SHOW NATIVE AD BETWEEN STORES !!!
        if (position == 3) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_small_native_ad, parent, false);
            final TemplateView storeNativeAdTemplate = convertView.findViewById(R.id.store_native_ad_template);
            AdLoader storeAdLoader = new AdLoader.Builder(getContext(), "ca-app-pub-1548831853802422/5931536302")
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                            storeNativeAdTemplate.setNativeAd(nativeAd);
                            storeNativeAdTemplate.setVisibility(View.VISIBLE);
                        }
                    })
                    .build();
            storeAdLoader.loadAd(new AdRequest.Builder().build());
        } else {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_store, parent, false);

            ImageView storeMainImageView = convertView.findViewById(R.id.storeMainImageView);
            TextView storeNameTextView = convertView.findViewById(R.id.storeNameTextView);
            TextView storeInfoTextView = convertView.findViewById(R.id.storeInfoTextView);
            TextView storeUidTextView = convertView.findViewById(R.id.storeUidTextView);
            TextView storeKeyTextView = convertView.findViewById(R.id.storeKeyTextView);

            FoodStore message = getItem(position);

            assert message != null;

            boolean isPhoto = message.getMainStoreImageUri() != null;

            storeUidTextView.setText(message.getStoreUid());

            storeNameTextView.setText(message.getStoreName());

            storeInfoTextView.setText(message.getStoreInfo());

            storeKeyTextView.setText(message.getStoreKey());

            if (isPhoto) {
                Glide.with(storeMainImageView.getContext())
                        .load(message.getMainStoreImageUri())
                        .centerCrop()
                        .into(storeMainImageView);
            } else {
                //test it
            }
        }

        return convertView;
    }
}
