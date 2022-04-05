package com.neftisoft.doyur;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LobbyActivity extends AppCompatActivity {

    FirebaseUser user;

    List<FoodLobby> foodLobbyList = new ArrayList<>();

    private AdView mLobbyAdView;

    private ListView mLobbyListView;
    private LobbyAdapter mLobbyAdapter;

    //Firebase Instance Variables
    private FirebaseDatabase mLobbyDatabase;
    private DatabaseReference mLobbyDatabaseRef;
    
    String username;
    String uid;

    Handler mHandler1 = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        // TO Change the status bar color!
        if (Build.VERSION.SDK_INT >= 21) { //For grey status bar!
            Window window = LobbyActivity.this.getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); //For grey status bar!
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); //For grey status bar!
            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(LobbyActivity.this,R.color.colorNavigationBar)); //For grey status bar!
        }

        //TO INITIALIZE ADMOB ADS!!!
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        //TO SHOW ADMOD ADS!!!
        mLobbyAdView = findViewById(R.id.lobby_ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        mLobbyAdView.loadAd(adRequest);

        FirebaseAuth mFirebaseAuth =FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();

        if (user != null) {
            username = user.getDisplayName();
            uid = user.getUid();
        }

        mLobbyDatabase = FirebaseDatabase.getInstance();
        mLobbyDatabaseRef = mLobbyDatabase.getReference().child("usersDatabase/" + uid + "/" + "lobbyDatabase/");

        // Initialize foods ListView and its adapter
        mLobbyListView = findViewById(R.id.lobbyListView);
        mLobbyAdapter = new LobbyAdapter(this, R.layout.item_lobby, foodLobbyList);
        mLobbyListView.setAdapter(mLobbyAdapter);
      
        /*
        TO LIST MESSAGES IN THE CORRECT ORDER (BY DATE) IN THE LOBBY!
         */
        Query theNewestMessageQuery = mLobbyDatabaseRef
                .orderByChild("/lobbyKey");
        theNewestMessageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot lobbyDataSnapshot, @Nullable String previousChildName) {
                final Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (lobbyDataSnapshot.hasChild("userName")
                                && lobbyDataSnapshot.hasChild("msg")
                                && lobbyDataSnapshot.hasChild("photoUrl2")
                                && lobbyDataSnapshot.hasChild("currentTime")
                                && lobbyDataSnapshot.hasChild("isNew")
                                && lobbyDataSnapshot.hasChild("lobbyKey")
                                && lobbyDataSnapshot.hasChild("uid")) {
                            FoodLobby foodLobby = lobbyDataSnapshot.getValue(FoodLobby.class);
                            mLobbyAdapter.add(foodLobby);
                            findViewById(R.id.lobby_progress_circle).setVisibility(View.GONE); //TO Hide progress bar
                            findViewById(R.id.lobby_no_msg_textview).setVisibility(View.GONE);
                            mHandler1.removeCallbacksAndMessages(null);
                        } else {
                            //mLob
                        }
                    }
                }, 50);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (mLobbyAdapter.isEmpty()) {
            mHandler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.lobby_progress_circle).setVisibility(View.GONE); //TO Hide progress bar
                    findViewById(R.id.lobby_no_msg_textview).setVisibility(View.VISIBLE);
                }
            },3500);
        }

        /*
        TO SEND USER TO CHAT ROOM !!!
         */
        mLobbyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView newMessageCount = view.findViewById(R.id.lobby_msg_counter_textview);
                newMessageCount.setVisibility(View.GONE);

                TextView childUid = view.findViewById(R.id.lobby_uid_textview);
                String lobbyUid = childUid.getText().toString();

                //TO Prevent showing new message icon when user opens lobby next time while there is no new msg !!!
                String otherUid1;
                String[] separated = lobbyUid.split("_");
                if (separated[0].equals(uid)) {
                    otherUid1 = separated[1];
                } else {
                    otherUid1 = separated[0];
                }
                DatabaseReference mLobbyDatabase1Ref = mLobbyDatabase.getReference().child("usersDatabase/" + uid + "/" + "lobbyDatabase/" + otherUid1 + "/");
                final DatabaseReference mLobbyDatabase1IsNewRef = mLobbyDatabase1Ref.child("isNew");
                mLobbyDatabase1IsNewRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            if (dataSnapshot.exists()) {
                                String isItNew = dataSnapshot.getValue().toString();
                                if (isItNew.equals("yes")) {
                                    mLobbyDatabase1IsNewRef.setValue("no");
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                if (user != null) {
                    Intent myIntent = new Intent(LobbyActivity.this, ChatActivity.class);

                    myIntent.putExtra("Lobby Uid", lobbyUid);

                    LobbyActivity.this.startActivity(myIntent);
                }
                else {
                    //To show custom toast message!
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Lütfen hesabınıza giriş yapın!");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });
        /*
        TO DELETE MESSAGE ITEM !!!
         */
        mLobbyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long r) {
                TextView childUid = view.findViewById(R.id.lobby_uid_textview);
                String lobbyUidToGetOtherUid = childUid.getText().toString();
                final String otherUid;
                String[] separated = lobbyUidToGetOtherUid.split("_");
                if (separated[0].equals(uid)) {
                    otherUid = separated[1];
                } else {
                    otherUid = separated[0];
                }
                AlertDialog.Builder builderReview = new AlertDialog.Builder(LobbyActivity.this);
                builderReview.setTitle("Mesajlaşmayı sil");
                builderReview.setMessage("\nBu mesajlaşmayı listeden kaldırmak istediğinize emin misiniz?\n");
                builderReview.setCancelable(true);

                builderReview.setPositiveButton(Html.fromHtml("<font color='#FF6347'>Sil</font>"),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                //TO Delete reviews from userDatabases !!!
                                foodLobbyList.remove(i); //TO Remove review from list
                                mLobbyAdapter.notifyDataSetChanged();
                                Query reviewsQuery = mLobbyDatabaseRef.orderByKey().equalTo(otherUid);
                                reviewsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot deletedItemSnapshot: dataSnapshot.getChildren()) {
                                            deletedItemSnapshot.getRef().removeValue(); //TO Remove review from database
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                                //To show custom toast message!
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.custom_toast,
                                        (ViewGroup) findViewById(R.id.custom_toast_container));
                                TextView text = layout.findViewById(R.id.toast_text);
                                text.setText("Mesajlaşma silindi!");
                                Toast toast = new Toast(getApplicationContext());
                                toast.setGravity(Gravity.BOTTOM, 0, 0);
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setView(layout);
                                toast.show();
                                dialog.cancel();
                            }
                        });
                builderReview.setNegativeButton(Html.fromHtml("<font color='#556B2F'>İptal</font>"),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertReview = builderReview.create();
                alertReview.show();
                return true;
            }
        });
    }

    /*
    TO GET BACK !!!
     */
    public void getBack(View view) {
        onBackPressed();
    }
}
