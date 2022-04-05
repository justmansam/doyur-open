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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

// IT IS FOR LOBBY ACTIVITY !!!

public class LobbyAdapter extends ArrayAdapter<FoodLobby> {
    public LobbyAdapter(Context context, int resource, List<FoodLobby> objects) {
        super(context, resource, objects);
    }

    private String uid;
    private String otherUid;
    private String lobbyUid;
    private String dateToShowInLobby;
    private String lobbyKey;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_lobby, parent, false);
        }

        FirebaseAuth mAuth;
        FirebaseUser user;
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        final FoodLobby message = getItem(position);

        // TO Show new message icon for once only if there is a new message !!!
        if (message != null && message.getUid() != null) { // && message.getUid() != null && message.getCurrentTime() != null && message.getMsg() != null && message.getUserName() != null && message.getLobbyKey() != null && message.getPhotoUrl2() != null

            RelativeLayout avatarRelativeLayout = convertView.findViewById(R.id.lobby_avatar_imageview_relative_layout);
            ImageView photoImageView = convertView.findViewById(R.id.lobby_avatar_imageview);
            final TextView nameTextView = convertView.findViewById(R.id.lobby_username_textview);
            final TextView msgTextView = convertView.findViewById(R.id.lobby_lastmsg_textview);
            TextView uidTextView = convertView.findViewById(R.id.lobby_uid_textview);
            final TextView msgTimeTextView = convertView.findViewById(R.id.lobby_time_textview);
            final TextView msgCounterTextView = convertView.findViewById(R.id.lobby_msg_counter_textview);
            TextView lobbyKeyTextView = convertView.findViewById(R.id.lobby_key_textview);
            final TextView msgStatusPendingTextView = convertView.findViewById(R.id.lobby_msg_status_pending_textview);
            final TextView msgStatusIsSeenTextView = convertView.findViewById(R.id.lobby_msg_status_isseen_textview);

            lobbyUid = message.getUid();
            String lobbyItemDateAndTime = message.getCurrentTime();
            StringTokenizer tokens = new StringTokenizer(lobbyItemDateAndTime, ",");
            String lobbyItemDate = tokens.nextToken().trim();
            String lobbyItemTime = tokens.nextToken().trim();

            //TO Get the date of today.
            String dateToday = new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date());
            //TO Get the date of yesterday.
            DateFormat dateOfYesterday = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String dateYesterday = dateOfYesterday.format(cal.getTime());

            if (lobbyUid != null) {
                String[] separated = lobbyUid.split("_");
                if (separated[0].equals(uid)) {
                    otherUid = separated[1];
                } else {
                    otherUid = separated[0];
                }
            }
            if (lobbyItemDate.equals(dateToday)) {
                dateToShowInLobby = "Bugün, " + lobbyItemTime;
            } else if (lobbyItemDate.equals(dateYesterday)) {
                dateToShowInLobby = "Dün, " + lobbyItemTime;
            } else {
                dateToShowInLobby = lobbyItemDateAndTime;
            }

            lobbyKey = message.getLobbyKey();

            FirebaseDatabase mLobbyDatabase = FirebaseDatabase.getInstance();
            DatabaseReference mLobbyDatabaseRef = mLobbyDatabase.getReference().child("usersDatabase/" + uid + "/" + "lobbyDatabase/" + otherUid + "/");
            DatabaseReference mOtherLobbyDatabaseRef = mLobbyDatabase.getReference().child("usersDatabase/" + otherUid + "/" + "lobbyDatabase/" + uid + "/");
            DatabaseReference mLobbyDatabaseIsNewRef2 = mLobbyDatabaseRef.child("isNew"); //TO Update lobby (alert) when a new message send!
            mLobbyDatabaseIsNewRef2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        String isNew = dataSnapshot.getValue().toString();
                        if (isNew.equals("yes")) {
                            msgCounterTextView.setVisibility(View.VISIBLE);
                        } else if (isNew.equals("no")) {
                            msgCounterTextView.setVisibility(View.GONE);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            DatabaseReference mLobbyDatabaseNewMessageRef = mLobbyDatabaseRef.child("msg"); //TO Update lobby (message) when a new message send!
            mLobbyDatabaseNewMessageRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        String newMsg = dataSnapshot.getValue().toString();
                        if (dataSnapshot.exists()) {
                            msgTextView.setText(newMsg);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            DatabaseReference mLobbyDatabaseDateRef = mLobbyDatabaseRef.child("currentTime"); //TO Update lobby (date&time) when a new message send!
            mLobbyDatabaseDateRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        String newTime = dataSnapshot.getValue().toString();
                        StringTokenizer tokens = new StringTokenizer(newTime, ",");
                        String lobbyItemDate = tokens.nextToken().trim();
                        String lobbyItemTime = tokens.nextToken().trim();
                        //TO Get the date of today.
                        String dateToday = new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date());
                        //TO Get the date of yesterday.
                        DateFormat dateOfYesterday = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, -1);
                        String dateYesterday = dateOfYesterday.format(cal.getTime());
                        if (dataSnapshot.exists()) {
                            if (lobbyItemDate.equals(dateToday)) {
                                dateToShowInLobby = "Bugün, " + lobbyItemTime;
                            } else if (lobbyItemDate.equals(dateYesterday)) {
                                dateToShowInLobby = "Dün, " + lobbyItemTime;
                            } else {
                                dateToShowInLobby = newTime;
                            }
                            msgTimeTextView.setText(dateToShowInLobby);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            DatabaseReference mLobbyDatabaseUserNameRef = mLobbyDatabaseRef.child("userName"); //TO Update lobby (message) when a new message send!
            mLobbyDatabaseUserNameRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        String newUserName = dataSnapshot.getValue().toString();
                        if (dataSnapshot.exists()) {
                            nameTextView.setText(newUserName);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            DatabaseReference mOtherLobbyDatabaseIsNewRef2 = mOtherLobbyDatabaseRef.child("isNew"); //TO Update lobby (msg status) when a new message send!
            mOtherLobbyDatabaseIsNewRef2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        String msgStatus = dataSnapshot.getValue().toString();
                        if (msgStatus.equals("yes")) {
                            msgStatusIsSeenTextView.setVisibility(View.GONE);
                            msgStatusPendingTextView.setVisibility(View.VISIBLE);
                        } else if (msgStatus.equals("no")) {
                            msgStatusIsSeenTextView.setVisibility(View.VISIBLE);
                            msgStatusPendingTextView.setVisibility(View.GONE);
                        } else if (msgStatus.equals("none")) {
                            msgStatusPendingTextView.setVisibility(View.GONE);
                            msgStatusIsSeenTextView.setVisibility(View.GONE);
                        }
                    } else { //IF OTHER USER DELETES THE MESSAGE IN THE LOBBY
                        msgStatusIsSeenTextView.setVisibility(View.GONE);
                        msgStatusPendingTextView.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            boolean isPhoto = message.getPhotoUrl2() != null;
            if (isPhoto) {
                Glide.with(photoImageView.getContext())
                        .load(message.getPhotoUrl2())
                        .centerCrop()
                        .into(photoImageView);
                avatarRelativeLayout.setBackground(null);
            }
            uidTextView.setText(message.getUid());
            nameTextView.setText(message.getUserName());
            msgTextView.setText(message.getMsg());
            msgTimeTextView.setText(message.getCurrentTime());
            msgTimeTextView.setText(dateToShowInLobby);
            lobbyKeyTextView.setText(lobbyKey);
        }
        return convertView;
    }
}
