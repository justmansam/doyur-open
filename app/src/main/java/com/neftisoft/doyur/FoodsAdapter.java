package com.neftisoft.doyur;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
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

// IT IS FOR MAIN SCREEN(PARTY) ACTIVITY (PICKING OR SHOOTING PHOTO AND SHOWING IT IN THE MAIN SCREEN) (CONNECTED TO THE ITEM_FOOD!!!)

public class FoodsAdapter extends ArrayAdapter<FoodParty> {
    public FoodsAdapter(Context context, int resource, List<FoodParty> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //TO SHOW NATIVE AD BETWEEN FOODS !!!
        if (position % 9 == 2) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_medium_native_ad, parent, false);

            //TO SHOW TEST ADS! //ca-app-pub-3940256099942544/2247696110
            final TemplateView foodNativeAdTemplate = convertView.findViewById(R.id.food_native_ad_template);
            AdLoader foodAdLoader = new AdLoader.Builder(getContext(), "ca-app-pub-1548831853802422/8557699648")
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                            foodNativeAdTemplate.setNativeAd(nativeAd);
                            foodNativeAdTemplate.setVisibility(View.VISIBLE);
                        }
                    })
                    .build();

            foodAdLoader.loadAd(new AdRequest.Builder().build());
        } else {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_food, parent, false);

            ImageView photoImageView = convertView.findViewById(R.id.photoImageView);
            TextView authorTextView = convertView.findViewById(R.id.nameTextView);
            TextView uidTextView = convertView.findViewById(R.id.uidTextView);
            TextView keyTextView = convertView.findViewById(R.id.keyTextView);
            TextView priceTextView = convertView.findViewById(R.id.priceTextView);

            FoodParty message = getItem(position);

            assert message != null;
            
            boolean isPhoto = message.getPhotoUrl() != null;

            authorTextView.setText(message.getName());

            uidTextView.setText(message.getUid());

            keyTextView.setText(message.getKey());

            priceTextView.setText(message.getPrice());

            if (isPhoto) {
                Glide.with(photoImageView.getContext())
                        .load(message.getPhotoUrl())
                        .centerCrop()
                        .into(photoImageView);
            } else {
                //test it
            }
        }

        return convertView;
    }
}
