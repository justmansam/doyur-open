package com.neftisoft.doyur;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

// IT IS FOR DESIRED FOOD FRAGMENT !!!

public class DesireAdapter extends ArrayAdapter<FoodDesired> {
    public DesireAdapter(Context context, int resource, List<FoodDesired> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_desired, parent, false);

            FoodDesired message = getItem(position);

            if (message != null && message.getName() != null) {
                // TO Show new message icon for once only if there is a new message !!!
                RelativeLayout avatarRelativeLayout = convertView.findViewById(R.id.desire_avatar_imageview_relative_layout);
                ImageView desireAvatarView = convertView.findViewById(R.id.desire_avatar_imageview);
                TextView desiredFoodTextView = convertView.findViewById(R.id.desired_foodname_textview);
                TextView desiredUsernameTextView = convertView.findViewById(R.id.desired_username_textview);
                TextView desireUidTextView = convertView.findViewById(R.id.desire_uid_textview);
                TextView desireKeyTextView = convertView.findViewById(R.id.desire_key_textview);

                boolean isPhoto = message.getAvatar() != null;
                if (isPhoto) {
                    Glide.with(desireAvatarView.getContext())
                            .load(message.getAvatar())
                            .centerCrop()
                            .into(desireAvatarView);
                    avatarRelativeLayout.setBackground(null);
                }

                desiredFoodTextView.setText(message.getDesire());
                desiredUsernameTextView.setText(message.getName());
                desireUidTextView.setText(message.getUid());
                desireKeyTextView.setText(message.getKey());
            } else {
                //Do something
            }
        }
        return convertView;
    }
}
