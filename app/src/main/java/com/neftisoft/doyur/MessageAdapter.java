package com.neftisoft.doyur;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class MessageAdapter extends ArrayAdapter<FoodMessage> {

    public MessageAdapter(Context context, int resource, List<FoodMessage> objects) {
        super(context, resource, objects);
    }

    private String myUid;
    private String chatUid;
    private String dateToShowInChatRoom;
    private String chatItemDateAndTime;
    private String chatItemDate;
    private String chatItemTime;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        FoodMessage chat = getItem(position);
        if (user != null) {
            myUid = user.getUid();
        }
        if (chat != null) {

            chatUid = chat.getUid3();
            chatItemDateAndTime = chat.getTimeNow1();
            StringTokenizer tokens = new StringTokenizer(chatItemDateAndTime, ",");
            chatItemDate = tokens.nextToken().trim();
            chatItemTime = tokens.nextToken().trim();

            //TO Get the date of today.
            String dateToday = new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date());
            //TO Get the date of yesterday.
            DateFormat dateOfYesterday = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String dateYesterday = dateOfYesterday.format(cal.getTime());

            if (chatItemDate.equals(dateToday)) {
                dateToShowInChatRoom = "Bugün, " + chatItemTime;
            } else if (chatItemDate.equals(dateYesterday)) {
                dateToShowInChatRoom = "Dün, " + chatItemTime;
            } else {
                dateToShowInChatRoom = chatItemDateAndTime;
            }

            if (chatUid.equals(myUid)) {
                convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_chat_my, parent, false);

                ImageView myChatPhotoImageView = convertView.findViewById(R.id.myChatPhotoImageView);
                TextView myChatMessageTextView = convertView.findViewById(R.id.myChatMessageTextView);
                TextView myChatTimeTextView = convertView.findViewById(R.id.myChatTimeTextView);
                TextView myMessageUidTextView = convertView.findViewById(R.id.myChatMessageUidTextView);
                TextView myMessageKeyTextView = convertView.findViewById(R.id.myChatItemKeyTextView);

                boolean isPhoto = chat.getPhotoUrl1() != null;
                if (isPhoto) {
                    myChatMessageTextView.setVisibility(View.GONE);
                    myChatPhotoImageView.setVisibility(View.VISIBLE);
                    Glide.with(myChatPhotoImageView.getContext())
                            .load(chat.getPhotoUrl1())
                            .into(myChatPhotoImageView);
                    myChatTimeTextView.setText(dateToShowInChatRoom);
                    myMessageUidTextView.setText(chat.getUid3());
                    myMessageKeyTextView.setText(chat.getKey1());
                } else {
                    myChatMessageTextView.setVisibility(View.VISIBLE);
                    myChatPhotoImageView.setVisibility(View.GONE);
                    myChatMessageTextView.setText(chat.getText1());
                    myChatTimeTextView.setText(dateToShowInChatRoom);
                    myMessageUidTextView.setText(chat.getUid3());
                    myMessageKeyTextView.setText(chat.getKey1());
                }
            } else if (!chatUid.equals(myUid)) {
                convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_chat_other, parent, false);

                ImageView chatPhotoImageView = convertView.findViewById(R.id.chatPhotoImageView);
                TextView chatMessageTextView = convertView.findViewById(R.id.chatMessageTextView);
                TextView chatTimeTextView = convertView.findViewById(R.id.chatTimeTextView);
                TextView messengerUidTextView = convertView.findViewById(R.id.chatMessageUidTextView);
                TextView messengerKeyTextView = convertView.findViewById(R.id.chatItemKeyTextView);
                TextView messengerUrlTextView = convertView.findViewById(R.id.chatItemPhotoUrlTextView);

                boolean isPhoto = chat.getPhotoUrl1() != null;
                if (isPhoto) {
                    chatMessageTextView.setVisibility(View.GONE);
                    chatPhotoImageView.setVisibility(View.VISIBLE);
                    Glide.with(chatPhotoImageView.getContext())
                            .load(chat.getPhotoUrl1())
                            .into(chatPhotoImageView);
                    chatTimeTextView.setText(dateToShowInChatRoom);
                    messengerUidTextView.setText(chat.getUid3());
                    messengerKeyTextView.setText(chat.getKey1());
                    messengerUrlTextView.setText(chat.getPhotoUrl1()); //TO SHOW OTHER USER'S IMAGE FULL SCREEN!
                } else {
                    chatMessageTextView.setVisibility(View.VISIBLE);
                    chatPhotoImageView.setVisibility(View.GONE);
                    chatMessageTextView.setText(chat.getText1());
                    chatTimeTextView.setText(dateToShowInChatRoom);
                    messengerUidTextView.setText(chat.getUid3());
                    messengerKeyTextView.setText(chat.getKey1());
                }
            }
        }
        return convertView;
    }
}
