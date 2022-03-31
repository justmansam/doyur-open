package com.neftisoft.doyur;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 160;
    private static final byte REQUEST_TAKE_PHOTO = 1;
    private static final byte RC_PHOTO_PICKER = 2;

    private byte alreadySentAnEmail = 0; // 0=NO, 1=YES

    private FirebaseUser user;

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ImageView mPhotoPickerButton;
    private EditText mMessageEditText;
    private TextView msgStatusTextView;

    private String chatImageFileName;
    private String currentPhotoPath;

    private String username;
    private String otherUsername;
    private String uid;
    private String otherUid;
    private String lobbyUid;
    private String otherUid1;
    private String childUid;
    private String alter1Uid;
    private String alter2Uid;
    private String timeNow;
    private String myAvatar;
    private String othersAvatar;
    private String otherEmail;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageRef;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;

    private String lastMsgUid;;
    private DatabaseReference mMessageStatusDBReference;

    private FirebaseDatabase mLobbyDatabase1;
    private DatabaseReference mLobbyDatabaseRef1;

    private DatabaseReference mLobbyDatabaseRef2;

    //Notification Initials
    private FirebaseDatabase mNotificationDatabase; //For notifications
    private DatabaseReference mNotificationDatabaseRef; //For last message and meseenger

    private FirebaseDatabase mUsersDatabase;  //To store, update and get user information
    private DatabaseReference mUsersDatabaseOthersRef;  //To store, update and get user information

    private Dialog dialog;

    Handler msgStatusHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // TO Change the status bar color!
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = ChatActivity.this.getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); //For grey status bar!
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); //For grey status bar!
            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(ChatActivity.this,R.color.colorNavigationBar)); //For grey status bar!
        }

        // User comes from PartyActivity (Daily / Charity) OR directly from StoreActivity
        Intent partyIntent = getIntent();
        otherUid = partyIntent.getStringExtra("Other Uid");
        otherUsername = partyIntent.getStringExtra("Other Username");
        setOthersUsername();

        // User comes from LobbyActivity
        Intent lobbyIntent = getIntent();
        lobbyUid = lobbyIntent.getStringExtra("Lobby Uid");

        // TO Initialize firebase components
        mFirebaseAuth = FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();

        if (user != null) {
            // Name, email address, and profile photo Url
            username = user.getDisplayName();
            //String email = user.getEmail();
            Uri avatarUri = user.getPhotoUrl();
            if (avatarUri != null) {
                myAvatar = avatarUri.toString();
            } else {
                //Default Avatar!
                myAvatar = "https://firebasestorage.$...$/defaultAvatarImage%2Favatar_icon.png$...$";
            }
            // Check if user's email is verified
            //boolean emailVerified = user.isEmailVerified();
            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            uid = user.getUid();
            //Download Images from Firebase to make any action on it (below)
        }

        msgStatusTextView = findViewById(R.id.message_status_textview);

        /*
        TO CHECK IF USER COMES FROM PARTY OR LOBBY !!!
         */
        if (lobbyUid != null) {
            childUid = lobbyUid;  //Updates childUid every time to see the correct chat room if user comes from Lobby.
            findViewById(R.id.chat_progress_circle).setVisibility(View.VISIBLE);
            updateChatRooom();
        } else {
            alter1Uid = otherUid + "_" + uid;  //To update childUid if user comes from Party !!!
            alter2Uid = uid + "_" + otherUid;  //To check if two people has already chat before !!! (AND OTHER USER IS THE STARTER OF THE CONVERSATION!)
            /*
            TO CHECK IF THERE IS ALREADY A CHAT ROOM FOR THIS TWO PEOPLE OR NOT!!!
             */
            FirebaseDatabase mChatRoomDatabase = FirebaseDatabase.getInstance();
            DatabaseReference mAlterChatRoomDatabase = mChatRoomDatabase.getReference().child("chatMessageDatabase/");
            Query alterQuery = mAlterChatRoomDatabase.orderByKey().equalTo(alter2Uid);
            alterQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        childUid = alter2Uid;
                        findViewById(R.id.chat_progress_circle).setVisibility(View.VISIBLE);
                        updateChatRooom();
                    } else {
                        childUid = alter1Uid;
                        updateChatRooom();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
        //TO Get which part of the lobby uid is OTHER UID !!!
        if (lobbyUid != null) {
            String[] separated = lobbyUid.split("_");
            if (separated[0].equals(uid)) {
                otherUid = separated[1];
            } else {
                otherUid = separated[0];
            }
        }

        //TO GET OTHER AVATAR READY IN ADVANCE!!!
        mUsersDatabase = FirebaseDatabase.getInstance();  //TO Get other user avatar uri.
        mUsersDatabaseOthersRef = mUsersDatabase.getReference().child("usersDatabase/" + otherUid + "/" + "vitalsDatabase/");  //TO Get other user avatar uri & username (below).
        DatabaseReference mUsersDatabaseRefOthersAvatar = mUsersDatabaseOthersRef.child("avatar");
        mUsersDatabaseRefOthersAvatar.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot111) {
                try {
                    if (dataSnapshot111.getValue() != null) {
                        try {
                            String othersAvatar1 = dataSnapshot111.getValue().toString();
                            setOthersAvatar(othersAvatar1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        /*
        TO GET OTHER USERNAME READY IN ADVANCE AND SHOW IT!!!
         */
        DatabaseReference mUsersDatabaseRefOthersName = mUsersDatabaseOthersRef.child("username");
        mUsersDatabaseRefOthersName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot222) {
                try {
                    if (dataSnapshot222.getValue() != null) {
                        try {
                            String otherUserName1 = dataSnapshot222.getValue().toString();
                            if (!otherUserName1.equals("")) {
                                otherUsername = otherUserName1;
                                setOthersUsername();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        /*
        TO GET OTHER USER EMAIL READY IN ADVANCE AND SHOW IT!!!
         */
        DatabaseReference mUsersDatabaseRefOthersEmail = mUsersDatabaseOthersRef.child("email");
        mUsersDatabaseRefOthersEmail.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot333) {
                try {
                    if (dataSnapshot333.getValue() != null) {
                        try {
                            String otherUserEmail = dataSnapshot333.getValue().toString();
                            if (!otherUserEmail.equals("")) {
                                otherEmail = otherUserEmail;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        /*
        TO GET OTHER USER LOCATION AND SHOW IT!!!
         */
        DatabaseReference mUsersDatabaseOthersLocationRef = mUsersDatabase.getReference().child("usersDatabase/" + otherUid + "/" + "locationDatabase/");
        mUsersDatabaseOthersLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot otherUserLocationSnapshot) {
                if (otherUserLocationSnapshot.hasChild("city") && otherUserLocationSnapshot.hasChild("county") && otherUserLocationSnapshot.hasChild("nbhood")) {
                    String chatRoomCityLocation;
                    String chatRoomCountyLocation;
                    String chatRoomNbhoodLocation;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        chatRoomCityLocation = Objects.requireNonNull(otherUserLocationSnapshot.child("city").getValue()).toString();
                        chatRoomCountyLocation = Objects.requireNonNull(otherUserLocationSnapshot.child("county").getValue()).toString();
                        chatRoomNbhoodLocation = Objects.requireNonNull(otherUserLocationSnapshot.child("nbhood").getValue()).toString();
                    } else {
                        chatRoomCityLocation = otherUserLocationSnapshot.child("city").getValue().toString();
                        chatRoomCountyLocation = otherUserLocationSnapshot.child("county").getValue().toString();
                        chatRoomNbhoodLocation = otherUserLocationSnapshot.child("nbhood").getValue().toString();
                    }
                    String chatRoomLocation = chatRoomCityLocation + ", " + chatRoomCountyLocation + ", " + chatRoomNbhoodLocation;
                    TextView chatRoomUserLocation = findViewById(R.id.chat_room_user_location_textview);
                    chatRoomUserLocation.setText(chatRoomLocation);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // TO Initialize references to views
        mMessageListView = findViewById(R.id.messageListView);
        mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        mMessageEditText = findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // TO Initialize message ListView and its adapter
        final List<FoodMessage> foodsMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_chat_other, foodsMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builderChatImage = new AlertDialog.Builder(ChatActivity.this);
                builderChatImage.setTitle("Fotoğraf gönder...");
                builderChatImage.setMessage("\n");
                builderChatImage.setCancelable(true);
                builderChatImage.setPositiveButton(
                        "Fotoğraf çek",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                // Ensure that there's a camera activity to handle the intent
                                if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                                    // Create the File where the photo should go
                                    File photoFile = null;
                                    try {
                                        photoFile = createChatImageFile(); //Take image file name for the avatar.
                                    } catch (IOException ex) {
                                        // Error occurred while creating the File
                                    }
                                    // Continue only if the File was successfully created
                                    if (photoFile != null) {
                                        Uri photoURI = FileProvider.getUriForFile(ChatActivity.this,
                                                "com.neftisoft.android.fileprovider",
                                                photoFile);
                                        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                        startActivityForResult(pictureIntent, REQUEST_TAKE_PHOTO);
                                    }
                                }
                                dialog.cancel();
                            }
                        });
                builderChatImage.setNegativeButton(
                        "Galeriden seç",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                pictureIntent.setType("image/jpeg");
                                pictureIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                startActivityForResult(Intent.createChooser(pictureIntent, "Complete action using"), RC_PHOTO_PICKER);
                                dialog.cancel();
                            }
                        });
                builderChatImage.setNeutralButton("İptal",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertChatImage = builderChatImage.create();
                alertChatImage.show();

                mMessageEditText.setText("");
            }
        });

        // TO Update Message Status for other user when user types something!
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    //mProgressBar.setVisibility(View.GONE);
                    FirebaseDatabase mMessageStatusDatabase = FirebaseDatabase.getInstance();
                    final DatabaseReference mMessageStatusDatabaseRef = mMessageStatusDatabase.getReference().child("usersDatabase/" + uid + "/" + "lobbyDatabase/" + otherUid + "/isNew");
                    mMessageStatusDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                String isItNew = snapshot.getValue().toString();
                                if (isItNew.equals("yes")) {
                                    mMessageStatusDatabaseRef.setValue("no");
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //TO Delete selected message OR TO SHOW SELECTED IMAGE IN FULL SCREEN! (Msg Deleting is CANCELED!)
        mMessageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                //TextView messageKeyToDelete = view.findViewById(R.id.myChatItemKeyTextView);
                TextView messageUrlToShowFullscreen = view.findViewById(R.id.chatItemPhotoUrlTextView);
                if (messageUrlToShowFullscreen != null) {
                    //TO SHOW OTHER USER'S IMAGE FULL SCREEN!
                    String messageUrlToShowFullscreenString = messageUrlToShowFullscreen.getText().toString();

                    if (!messageUrlToShowFullscreenString.equals("message")) {
                        Intent myIntent = new Intent(ChatActivity.this, FullscreenImageActivity.class);
                        myIntent.putExtra("Image Uri", messageUrlToShowFullscreenString);
                        ChatActivity.this.startActivity(myIntent);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent pictureIntent) { //DEVELOPER ANDROID
        super.onActivityResult(requestCode, resultCode, pictureIntent);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            addPhotoShoot();
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = pictureIntent.getData();
            addPhotoPicked(selectedImageUri);
        }
    }

    /*
    TO GET BACK !!!
     */
    public void getBack(View view) {
        onBackPressed();
    }

    /*
    TO LISTEN CHANGES IN THE CHAT ROOM !!!
     */
    private void updateChatRooom() {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mChatPhotosStorageRef = mFirebaseStorage.getReference().child("chatPhotoStorage/" + childUid + "/");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessageDatabaseReference = mFirebaseDatabase.getReference().child("chatMessageDatabase/" + childUid + "/");

        //TO Show messages!!!
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                FoodMessage foodMessage = dataSnapshot.getValue(FoodMessage.class);
                mMessageAdapter.add(foodMessage);
                findViewById(R.id.chat_progress_circle).setVisibility(View.GONE); //TO Hide progress bar
                //allreadyChatBefore = 1;

                msgStatusHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            lastMsgUid = Objects.requireNonNull(dataSnapshot.child("uid3").getValue()).toString();
                        } else {
                            lastMsgUid = dataSnapshot.child("uid3").getValue().toString();
                        }
                        if (lastMsgUid.equals(uid)) {
                            updateMessageStatus();
                        } else {
                            msgStatusTextView.setVisibility(View.GONE);
                        }
                    }
                }, 500);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        mMessageDatabaseReference.addChildEventListener(mChildEventListener);
    }

    /*
    TO LISTEN THE STATUS OF THE LAST MESSAGE (WHETHER IT IS SEEN OR NOT!) !!!
     */
    private void updateMessageStatus() {
        mMessageStatusDBReference = mFirebaseDatabase.getReference().child("usersDatabase/" + otherUid + "/" + "lobbyDatabase/" + uid + "/" + "isNew");
        mMessageStatusDBReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot msgStatusSnapshot) {
                String isNotSeen;
                if (msgStatusSnapshot.exists()) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        isNotSeen = Objects.requireNonNull(msgStatusSnapshot.getValue()).toString();
                    } else {
                        isNotSeen = msgStatusSnapshot.getValue().toString();
                    }
                    if (isNotSeen.equals("yes") && lastMsgUid.equals(uid)) {
                        //
                        msgStatusTextView.setVisibility(View.VISIBLE);
                        msgStatusTextView.setBackgroundResource(R.drawable.custom_chat_msg_status_pending_background);
                        msgStatusTextView.setText("Gönderildi \u2713");
                    } else if (isNotSeen.equals("no") && lastMsgUid.equals(uid)) {
                        msgStatusTextView.setVisibility(View.VISIBLE);
                        msgStatusTextView.setBackgroundResource(R.drawable.custom_chat_msg_status_isseen_background);
                        msgStatusTextView.setText("Görüldü \u2713\u2713");
                    }
                } else {
                    msgStatusTextView.setVisibility(View.VISIBLE);
                    msgStatusTextView.setBackgroundResource(R.drawable.custom_chat_msg_status_pending_background);
                    msgStatusTextView.setText("Gönderildi \u2713");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /*
    TO SEND MESSAGE !!!
     */
    public void sendMessage(View view) {
        EditText messageEditText = findViewById(R.id.messageEditText);
        String message = messageEditText.getText().toString();
        if (message.length() > 0) {
            Calendar currentTime = Calendar.getInstance();
            int hour                = currentTime.get(Calendar.HOUR_OF_DAY) ;
            int minute              = currentTime.get(Calendar.MINUTE)      ;
            int second              = currentTime.get(Calendar.SECOND)      ;
            String [] ids           = TimeZone.getAvailableIDs()            ;
            long milliDiff          = currentTime.get(Calendar.ZONE_OFFSET) ;
            for (String id : ids)
            {
                TimeZone tz = TimeZone.getTimeZone(id)                   ;
                if (tz.getRawOffset() == milliDiff)
                {  // Found a match, now check for daylight saving
                    boolean inDs    = tz.inDaylightTime(new Date())   ;
                    if (inDs)       { hour += 1 ; }
                    if (hour == 25) { hour  = 1 ; }
                    break                                             ;
                }
            }
            String minuteString;
            if (minute < 10) {
                timeNow = String.valueOf(hour + ":" + "0" + minute);
                minuteString = "0" + minute;
            }
            else {
                timeNow = String.valueOf(hour + ":" + minute);
                minuteString = String.valueOf(minute);
            }
            String secondString;
            if (second < 10) {
                secondString = "0" + second;
            } else {
                secondString = String.valueOf(second);
            }

            String ownMessage = "Siz: " + message;
            String chatDate = new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date());
            timeNow = chatDate + ", " + timeNow;
            
            //Magic is for listing upside down!!!
            String itemDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
            String itemTime = hour + minuteString + secondString;
            String itemKey;
            int inverseItemDateInt = 99991231 - Integer.parseInt(itemDate);
            int inverseItemTimeInt = 246060 - Integer.parseInt(itemTime);
            if (hour > 14) {
                itemKey = inverseItemDateInt + "_0" + inverseItemTimeInt;
            } else {
                itemKey = inverseItemDateInt + "_" + inverseItemTimeInt;
            }

            /*
            TO SEND ITEMS TO USER'S LOBBIES !!!
             */
            mLobbyDatabase1 = FirebaseDatabase.getInstance(); //LobbyActivity!!
            //The first one (User itself)
            mLobbyDatabaseRef1 = mLobbyDatabase1.getReference().child("usersDatabase/" + uid + "/" + "lobbyDatabase/" + otherUid + "/");
            DatabaseReference mLobbyDatabaseNameRef1 = mLobbyDatabaseRef1.child("userName");
            DatabaseReference mLobbyDatabaseMessageRef1 = mLobbyDatabaseRef1.child("msg");
            DatabaseReference mLobbyDatabaseAvatarRef1 = mLobbyDatabaseRef1.child("photoUrl2");
            DatabaseReference mLobbyDatabaseDateRef1 = mLobbyDatabaseRef1.child("currentTime");
            DatabaseReference mLobbyDatabaseIsNewRef1 = mLobbyDatabaseRef1.child("isNew");
            DatabaseReference mLobbyDatabaseKeyRef1 = mLobbyDatabaseRef1.child("lobbyKey");
            DatabaseReference mLobbyDatabaseLobbyUidRef1 = mLobbyDatabaseRef1.child("uid");
            mLobbyDatabaseNameRef1.setValue(otherUsername);
            mLobbyDatabaseMessageRef1.setValue(ownMessage);
            mLobbyDatabaseAvatarRef1.setValue(othersAvatar);
            mLobbyDatabaseDateRef1.setValue(timeNow);
            mLobbyDatabaseIsNewRef1.setValue("none");
            mLobbyDatabaseKeyRef1.setValue(itemKey);
            mLobbyDatabaseLobbyUidRef1.setValue(childUid);
            //The second one (Other User)
            mLobbyDatabaseRef2 = mLobbyDatabase1.getReference().child("usersDatabase/" + otherUid + "/" + "lobbyDatabase/" + uid + "/");
            DatabaseReference mLobbyDatabaseNameRef2 = mLobbyDatabaseRef2.child("userName");
            DatabaseReference mLobbyDatabaseMessageRef2 = mLobbyDatabaseRef2.child("msg");
            DatabaseReference mLobbyDatabaseAvatarRef2 = mLobbyDatabaseRef2.child("photoUrl2");
            DatabaseReference mLobbyDatabaseDateRef2 = mLobbyDatabaseRef2.child("currentTime");
            DatabaseReference mLobbyDatabaseIsNewRef2 = mLobbyDatabaseRef2.child("isNew");
            DatabaseReference mLobbyDatabaseKeyRef2 = mLobbyDatabaseRef2.child("lobbyKey");
            DatabaseReference mLobbyDatabaseLobbyUidRef2 = mLobbyDatabaseRef2.child("uid");
            mLobbyDatabaseNameRef2.setValue(username);
            mLobbyDatabaseMessageRef2.setValue(message);
            mLobbyDatabaseAvatarRef2.setValue(myAvatar);
            mLobbyDatabaseDateRef2.setValue(timeNow);
            mLobbyDatabaseIsNewRef2.setValue("yes");
            mLobbyDatabaseKeyRef2.setValue(itemKey);
            mLobbyDatabaseLobbyUidRef2.setValue(childUid);

            /*
            TO SEND NOTIFICATION !!!
             */
            //TO Divide childUid like before and after "_"
            StringTokenizer tokens = new StringTokenizer(childUid, "_");
            String uid1 = tokens.nextToken().trim();
            String uid2 = tokens.nextToken().trim();
            //TO Find out which uid matches to user uid.
            if (uid1.equals(uid)) {
                otherUid1 = uid2;
            } else if (uid2.equals(uid)) {
                otherUid1 = uid1;
            }
            mNotificationDatabase = FirebaseDatabase.getInstance();
            mNotificationDatabaseRef = mNotificationDatabase.getReference().child("usersDatabase/" + otherUid1 + "/" + "notificationDatabase" + "/");
            final DatabaseReference mNotificationDatabaseRefLastMessage = mNotificationDatabaseRef.child("Message");
            final DatabaseReference mNotificationDatabaseRefLastMessenger = mNotificationDatabaseRef.child("Messenger");

            //TO Send message values to the child references.
            FoodMessage foodMessage = new FoodMessage(mMessageEditText.getText().toString(), username, null, timeNow, uid, itemKey);
            mMessageDatabaseReference.push().setValue(foodMessage);

            //TO Show the messenger and the last message in the notification.
            String notificationMessage = message;
            if (notificationMessage.length() > 40) {
                notificationMessage = notificationMessage.substring(0,40)+"...";
            }
            mNotificationDatabaseRefLastMessenger.setValue(username);
            mNotificationDatabaseRefLastMessage.setValue(notificationMessage);

            //TO SEND EMAIL (ONCE FOR EVERY FIRST ACTIVITY OPENING)
            if (alreadySentAnEmail == 0) {
                sendEmailToOtherUser(message, timeNow, username, uid, otherUid);
                alreadySentAnEmail = 1;
            }

            // TO Clear input box
            mMessageEditText.setText("");
        }
    }

    /*
    TO CREATE IMAGE FILE
     */
    private File createChatImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        // Create an image file name
        chatImageFileName = "JPEG_" + timeStamp + "_" + uid;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File chatImage = File.createTempFile(
                chatImageFileName,   /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = chatImage.getAbsolutePath();
        return chatImage;
    }

    /*
    TO ADD PHOTO TAKEN TO FIREBASE STORAGE
     */
    private void addPhotoShoot() {

        dialog = ProgressDialog.show(ChatActivity.this, "",
                "Resim gönderiliyor...\nLütfen bekleyin!", true);

        final Uri file = Uri.fromFile(new File(currentPhotoPath));
        final StorageReference mchatPhotosStorageRef = mChatPhotosStorageRef.child(chatImageFileName);

        mchatPhotosStorageRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        /*
                        Toast.makeText(ProfileActivity.this, "Profil resminiz başarıyla güncellendi!",
                                Toast.LENGTH_SHORT).show();
                                */
                        //Show uploaded photo in the main screen

                        // Get a URL to the uploaded content
                        getDownloadUrl(mchatPhotosStorageRef, file);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        dialog.cancel();

                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("Gönderme başarısız oldu! Lütfen tekrar deneyin.");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                        // ...
                    }
                });
    }

    /*
    TO ADD PHOTO PICKED TO FIREBASE STORAGE
     */
    private void addPhotoPicked(Uri selectedImageUri) {

        dialog = ProgressDialog.show(ChatActivity.this, "",
                "Resim gönderiliyor...\nLütfen bekleyin!", true);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        final Uri file = selectedImageUri; //selected image uri
        final StorageReference mchatPhotosStorageRef = mChatPhotosStorageRef.child("JPEG_" + timeStamp + "_" + uid);

        mchatPhotosStorageRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        /*
                        Toast.makeText(ChatActivity.this, "Resim gönderildi! Yüklenmesi bir kaç saniyeyi bulabilir.",
                                Toast.LENGTH_SHORT).show();
                                */
                        //Show uploaded photo in the main screen

                        // Get a URL to the uploaded content
                        getDownloadUrl(mchatPhotosStorageRef, file);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        dialog.cancel();
                        //To show custom toast message!
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_container));
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("Gönderme başarısız oldu! Lütfen tekrar deneyin.");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                        // ...
                    }
                });
    }

    private void getDownloadUrl(final StorageReference mChatPhotosStorageRef, Uri file) {

        UploadTask uploadTask = mChatPhotosStorageRef.putFile(file);

        final Task<Uri> photoUrl = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                final StorageReference[] mChatPhotoStorageDownloadRef = {null};
                final Task<Uri>[] downloadChatPhotoUri = new Task[1];
                if (!task.isSuccessful()) {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Beklenmedik bir hata oluştu! Lütfen tekrar deneyin.");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                    throw task.getException();
                } else {
                    String mChatPhotoStorageUploadChildRefString = mChatPhotosStorageRef.getName();
                    mChatPhotoStorageUploadChildRefString = mChatPhotoStorageUploadChildRefString + "_1024x1024";
                    StorageReference mChatPhotoStorageUploadRootRef = mChatPhotosStorageRef.getParent();
                    Thread.sleep(2100); //WAIT FOR THE FIREBASE FUNCTION TO RESIZE THE IMAGE!!!
                    mChatPhotoStorageDownloadRef[0] = mChatPhotoStorageUploadRootRef.child(mChatPhotoStorageUploadChildRefString);
                    downloadChatPhotoUri[0] = mChatPhotoStorageDownloadRef[0].getDownloadUrl();
                    return downloadChatPhotoUri[0];
                }
                // Continue with the task to get the download URL
                //return mchatPhotosStorageRef.getDownloadUrl(); //OLD ONE
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    getUrlandShowIt(downloadUri);

                } else {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Beklenmedik bir hata oluştu! Lütfen tekrar deneyin.");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });
    }

    //TO Show only the picture !
    private void getUrlandShowIt(Uri downloadUri) {
        
        /*
        TO SEND NOTIFICATION !!!
         */
        //TO Divide childUid like before and after "_"
        StringTokenizer tokens = new StringTokenizer(childUid, "_");
        String uid1 = tokens.nextToken().trim();
        String uid2 = tokens.nextToken().trim();

        //TO Find out which uid matches to user uid.
        if (uid1.equals(uid)) {
            otherUid1 = uid2;
        } else if (uid2.equals(uid)) {
            otherUid1 = uid1;
        }

        //TO Get the date and current time.
        Calendar currentTime = Calendar.getInstance();
        int hour                = currentTime.get(Calendar.HOUR_OF_DAY) ;
        int minute              = currentTime.get(Calendar.MINUTE)      ;
        int second              = currentTime.get(Calendar.SECOND)      ;
        String [] ids           = TimeZone.getAvailableIDs()            ;
        long milliDiff          = currentTime.get(Calendar.ZONE_OFFSET) ;
        for (String id : ids)
        {
            TimeZone tz = TimeZone.getTimeZone(id)                   ;
            if (tz.getRawOffset() == milliDiff)
            {  // Found a match, now check for daylight saving
                boolean inDs    = tz.inDaylightTime(new Date())   ;
                if (inDs)       { hour += 1 ; }
                if (hour == 25) { hour  = 1 ; }
                break                                             ;
            }
        }
        String minuteString;
        if (minute < 10) {
            timeNow = String.valueOf(hour + ":" + "0" + minute);
            minuteString = "0"+minute;
        }
        else {
            timeNow = String.valueOf(hour + ":" + minute);
            minuteString = String.valueOf(minute);
        }
        String secondString;
        if (second < 10) {
            secondString = "0"+second;
        } else {
            secondString = String.valueOf(second);
        }

        String chatDate = new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date());
        timeNow = chatDate + ", " + timeNow;

        //Magic is for listing upside down!!!
        String itemDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String itemTime = hour + minuteString + secondString;
        String itemKey;
        int inverseItemDateInt = 99991231 - Integer.parseInt(itemDate);
        int inverseItemTimeInt = 246060 - Integer.parseInt(itemTime);
        if (hour > 14) {
            itemKey = inverseItemDateInt + "_0" + inverseItemTimeInt;
        } else {
            itemKey = inverseItemDateInt + "_" + inverseItemTimeInt;
        }

        mNotificationDatabase = FirebaseDatabase.getInstance();
        mNotificationDatabaseRef = mNotificationDatabase.getReference().child("usersDatabase/" + otherUid1 + "/" + "notificationDatabase" + "/");
        final DatabaseReference mNotificationDatabaseRefLastMessage = mNotificationDatabaseRef.child("Message");
        final DatabaseReference mNotificationDatabaseRefLastMessenger = mNotificationDatabaseRef.child("Messenger");

        //TO Update chat lobby !!!
        mLobbyDatabase1 = FirebaseDatabase.getInstance(); //LobbyActivity!!
        //Sender Lobby.
        String ownMessage1 = "Siz: \uD83D\uDCF7 Fotoğraf!";
        mLobbyDatabaseRef1 = mLobbyDatabase1.getReference().child("usersDatabase/" + uid + "/" + "lobbyDatabase/" + otherUid1 + "/");
        DatabaseReference mLobbyDatabaseNameRef1 = mLobbyDatabaseRef1.child("userName");
        DatabaseReference mLobbyDatabaseMessageRef1 = mLobbyDatabaseRef1.child("msg");
        DatabaseReference mLobbyDatabaseAvatarRef1 = mLobbyDatabaseRef1.child("photoUrl2");
        DatabaseReference mLobbyDatabaseDateRef1 = mLobbyDatabaseRef1.child("currentTime");
        DatabaseReference mLobbyDatabaseIsNewRef1 = mLobbyDatabaseRef1.child("isNew");
        DatabaseReference mLobbyDatabaseKeyRef1 = mLobbyDatabaseRef1.child("lobbyKey");
        DatabaseReference mLobbyDatabaseLobbyUidRef1 = mLobbyDatabaseRef1.child("uid");
        mLobbyDatabaseNameRef1.setValue(otherUsername);
        mLobbyDatabaseMessageRef1.setValue(ownMessage1);
        mLobbyDatabaseAvatarRef1.setValue(othersAvatar);
        mLobbyDatabaseDateRef1.setValue(timeNow);
        mLobbyDatabaseIsNewRef1.setValue("no");
        mLobbyDatabaseKeyRef1.setValue(itemKey);
        mLobbyDatabaseLobbyUidRef1.setValue(childUid);
        //Receiver Lobby.
        mLobbyDatabaseRef2 = mLobbyDatabase1.getReference().child("usersDatabase/" + otherUid1 + "/" + "lobbyDatabase/" + uid + "/");
        DatabaseReference mLobbyDatabaseNameRef2 = mLobbyDatabaseRef2.child("userName");
        DatabaseReference mLobbyDatabaseMessageRef2 = mLobbyDatabaseRef2.child("msg");
        DatabaseReference mLobbyDatabaseAvatarRef2 = mLobbyDatabaseRef2.child("photoUrl2");
        DatabaseReference mLobbyDatabaseDateRef2 = mLobbyDatabaseRef2.child("currentTime");
        DatabaseReference mLobbyDatabaseIsNewRef2 = mLobbyDatabaseRef2.child("isNew");
        DatabaseReference mLobbyDatabaseKeyRef2 = mLobbyDatabaseRef2.child("lobbyKey");
        DatabaseReference mLobbyDatabaseLobbyUidRef2 = mLobbyDatabaseRef2.child("uid");
        mLobbyDatabaseNameRef2.setValue(username);
        mLobbyDatabaseMessageRef2.setValue("\uD83D\uDCF7 Fotoğraf!");
        mLobbyDatabaseAvatarRef2.setValue(myAvatar);
        mLobbyDatabaseDateRef2.setValue(timeNow);
        mLobbyDatabaseIsNewRef2.setValue("yes");
        mLobbyDatabaseKeyRef2.setValue(itemKey);
        mLobbyDatabaseLobbyUidRef2.setValue(childUid);

        //TO Send notification.
        mNotificationDatabaseRefLastMessenger.setValue(username);
        mNotificationDatabaseRefLastMessage.setValue("\uD83D\uDCF7 Fotoğraf!");

        //TO SEND EMAIL (ONCE FOR EVERY FIRST ACTIVITY OPENING)
        if (alreadySentAnEmail == 0) {
            sendEmailToOtherUser("\uD83D\uDCF7 Fotoğraf!", timeNow, username, uid, otherUid1);
            alreadySentAnEmail = 1;
        }

        //TO Send message values to the child references.
        FoodMessage foodMessage = new FoodMessage(null, username, downloadUri.toString(), timeNow, uid, itemKey);
        mMessageDatabaseReference.push().setValue(foodMessage);

        dialog.cancel();

        //To show custom toast message!
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText("Resim gönderildi! Yüklenmesi bir kaç saniyeyi bulabilir.");
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private void setOthersUsername() {
        TextView chatRoomUserName = findViewById(R.id.chat_room_user_name_textview);
        chatRoomUserName.setText(otherUsername);
    }
    private void setOthersAvatar(String othersAvatar1) {
        if (othersAvatar1 != null) {
            othersAvatar = othersAvatar1;
            ImageView chatAvatarImageView = findViewById(R.id.chat_room_avatar_imageview);
            Glide.with(chatAvatarImageView.getContext())
                    .load(othersAvatar)
                    .centerCrop()
                    .into(chatAvatarImageView);
            findViewById(R.id.chat_room_avatar_layout).setBackground(null);
        }
    }

    public void gotoProfileFromChat(View view) {
        Intent myIntent = new Intent(ChatActivity.this, ProfileActivity.class);
        myIntent.putExtra("Chatter Uid", otherUid);
        ChatActivity.this.startActivity(myIntent);
    }

    private void sendEmailToOtherUser(String chatMessage, String date, String name, String uid, String otherUid) {
        FirebaseFirestore eMailForChatFirestoreDatabase = FirebaseFirestore.getInstance();

        Map<String, Object> message = new HashMap<>();
        message.put("subject", "Yeni bir mesajın var!");
        message.put("html", "<p><b>" + name + "</b> sana ‘doyur’ üzerinden <b>" + date + "</b> tarihinde yeni bir mesaj gönderdi!</p>\n" +
                "-\n" +
                "<cite>\"<b>"+chatMessage+"</b>\"</cite>\n" +
                "-\n" +
                "\n" +
                "<p><br>Mutlu alışverişler!<br></p>\n" +
                "\n" +
                "<img src=\"https://firebasestorage.$...$.com/o/$...$doyur_logo_original.png$...$\" alt=\"doyur\" style=\"width:100px\">");

        Map<String, Object> chatMail = new HashMap<>();
        chatMail.put("to", otherEmail);
        chatMail.put("message", message);

        CollectionReference eMailForChatFirestoreCollectionRef = eMailForChatFirestoreDatabase.collection("mail");
        DocumentReference eMailForChatFirestoreDocumentRef = eMailForChatFirestoreCollectionRef.document(uid + "_" + otherUid);

        //dbDcRef.update(mail);
        eMailForChatFirestoreDocumentRef.delete();
        eMailForChatFirestoreDocumentRef.set(chatMail);
    }
}
