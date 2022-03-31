package com.neftisoft.doyur;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FullscreenImageActivity extends AppCompatActivity {

    private String imageUri;

    ImageView fullscreenImageView;
    ImageView fullscreenIconImageView;
    TextView fullscreenTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        // To change the status bar color!
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = FullscreenImageActivity.this.getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(FullscreenImageActivity.this,R.color.colorNavigationBar));
        }

        // User comes from either StoreActivity or ChatActivity!
        Intent partyIntent = getIntent();
        imageUri = partyIntent.getStringExtra("Image Uri");

        fullscreenImageView = findViewById(R.id.food_fullscreen_image_imageview);
        fullscreenIconImageView = findViewById(R.id.food_fullscreen_icon_imageview);
        fullscreenTextView = findViewById(R.id.food_fullscreen_textview);

        setFullscreenImage();
    }

    private void setFullscreenImage() {
        Uri imageUrl = Uri.parse(imageUri);
        Glide.with(fullscreenImageView.getContext())
                .load(imageUrl)
                .centerCrop()
                .into(fullscreenImageView);
    }

    /*
    TO GET BACK !!!
     */
    public void getBack(View view) {
        onBackPressed();
    }
}
